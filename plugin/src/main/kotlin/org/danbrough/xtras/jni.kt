package org.danbrough.xtras

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File

fun Project.xtrasJniConfig(
  namespace: String = group.toString(),
  compileSdk: Int = 34,
  block: LibraryExtension.() -> Unit = {}
) {
  extensions.getByType<LibraryExtension>().apply {
    this.compileSdk = compileSdk
    this.namespace = namespace

    defaultConfig {
      minSdk = 22
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
      sourceCompatibility = project.xtras.javaVersion
      targetCompatibility = project.xtras.javaVersion
    }

    sourceSets["debug"].jniLibs {
      srcDir(project.file("src/jniLibs/debug"))
    }

    sourceSets["release"].jniLibs {
      srcDir(project.file("src/jniLibs/release"))
    }

    block()
  }

  /**
   * Configure shared libraries to be copied to src/jniLibs/(debug|release)/(androidAbi)] after linking
   */
  tasks.withType<KotlinNativeLink> {
    val konanTarget = binary.target.konanTarget
    if (konanTarget.family == Family.ANDROID) {
      val libsDir = outputs.files.files.first()
      val jniLibsDir =
        file("src/jniLibs/${if (binary.buildType.debuggable) "debug" else "release"}/${konanTarget.androidLibDir}")
      val taskCopyName = "${name}_copyToJniLibs"
      tasks.register<Copy>(taskCopyName) {
        doFirst {
          logDebug("copying files from $libsDir to $jniLibsDir for ${this@withType.name}")
        }
        from(libsDir)
        into(jniLibsDir)
      }
      finalizedBy(taskCopyName)
    }
  }

  /**
   * Configure KotlinJvmTest task executions to find shared libraries
   */
  afterEvaluate {
    val linkTasks = tasks.filter {
      it is KotlinNativeLink &&
          it.binary is SharedLibrary &&
          it.binary.target.konanTarget == HostManager.host
          && it.binary.buildType == NativeBuildType.DEBUG
    }
    tasks.withType<KotlinJvmTest> {
      println("XTRAS_CONFIGURE: task $name depends on ${linkTasks.joinToString(" ") { it.name }}")
      dependsOn(*linkTasks.toTypedArray())
      val libPath =
        linkTasks.flatMap { it.outputs.files.files.filter { file -> file.isDirectory } }
          .joinToString(File.pathSeparator)
      project.logDebug("task:$name setting env:${HostManager.host.envLibraryPathName} to $libPath")
      environment(HostManager.host.envLibraryPathName, libPath)
    }

    tasks.withType<JavaExec> {
      project.logDebug("configuring JavaExec: $name: Adding link tasks ${linkTasks.joinToString(",") { it.name }}")

      dependsOn(*linkTasks.toTypedArray())
      val libPath =
        linkTasks.flatMap { it.outputs.files.files.filter { file -> file.isDirectory } }
          .joinToString(File.pathSeparator)
      project.logDebug("task:$name setting env:${HostManager.host.envLibraryPathName} to $libPath")
      environment(HostManager.host.envLibraryPathName, libPath)
    }
  }
}