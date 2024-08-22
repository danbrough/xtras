package org.danbrough.xtras

import org.danbrough.xtras.tasks.PackageTaskName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File


class XtrasPlugin : Plugin<Any> {
  override fun apply(target: Any) {
    if (target !is Project) return
    target.run {
      if (parent != null) error("Xtras plugin should be applied to the root project only")

      //logInfo("XtrasPlugin.apply() project:${target.path} parent: ${parent?.name}")

      allprojects {
        logInfo("applying XtrasPlugin")
        apply<MavenPublishPlugin>()
        apply<SigningPlugin>()

        val xtras = xtrasExtension

        xtras.nativeTargets.convention(providers.provider {
          val kotlin = extensions.findByName("kotlin")
          if (kotlin is KotlinMultiplatformExtension) {
            kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget }
          } else emptyList()
        })


        xtras.repoIDFileName.convention(project.provider {
          "sonatypeRepoID_${rootProject.name}_${rootProject.group}"
        })
        //by default share a single repoID for entire project

        xtras.repoIDFile.convention(xtras.repoIDFileName.map {
          rootProject.layout.buildDirectory.file(it).get()
        })

        (System.getenv("ANDROID_NDK") ?: System.getenv("ANDROID_NDK_ROOT"))?.also {
          xtras.androidConfig.ndkDir = File(it)
        }

        val xtrasJvmTarget = xtras.jvmTarget

        afterEvaluate {

          tasks.withType<KotlinJvmCompile> {
            compilerOptions {
              this.jvmTarget = xtrasJvmTarget
            }
          }

          registerMiscTasks()
        }

        configureExtras(xtras)
      }
    }
  }

}

val Project.xtrasExtension: Xtras
  get() = extensions.findByType<Xtras>() ?: extensions.create(
    XTRAS_EXTENSION_NAME,
    Xtras::class.java
  )

internal fun Project.registerMiscTasks() {

  val kotlin = extensions.findByName("kotlin")

  if (kotlin is KotlinMultiplatformExtension) {
    tasks.register("xtrasTargets") {
      group = XTRAS_TASK_GROUP
      description = "Lists all of the active kotlin targets"

      doFirst {
        kotlin.targets.all {
          logInfo("${project.group}.${project.name} -> target: $targetName")
        }
      }
    }
  }
}

private fun Project.configureExtras(xtras: Xtras) {
  //logDebug("configureExtras(): $name")

  findProperty(Xtras.Constants.Properties.PROJECT_GROUP)?.also {
    group = it.toString()
  } ?: logTrace("${Xtras.Constants.Properties.PROJECT_GROUP} not specified. Defaulting to $group")

  findProperty(Xtras.Constants.Properties.PROJECT_VERSION)?.also {
    version = it.toString()
  }
    ?: logTrace("${Xtras.Constants.Properties.PROJECT_VERSION} not specified. Defaulting to $version")

  logDebug("name:$name $group:$version")

  xtrasPublishing()

  registerKonanDepsTasks()

  configureProjectTasks(xtras)
}

/**
 * Setup environment for executable and test tasks
 */
private fun Project.configureProjectTasks(xtras: Xtras) {

  val prepareJniTask = tasks.create<Task>(Xtras.Constants.TaskNames.XTRAS_PREPARE_JNI_LIBS) {
    group = XTRAS_TASK_GROUP
    description = "Copy required files into jniLibs dir for android packaging"
  }

  afterEvaluate {
    logDebug("configureProjectTasks()")

    tasks.findByName("mergeReleaseJniLibFolders")?.dependsOn(prepareJniTask)

    val exes = kotlinBinaries { it is Executable && it.runsOnHost }
    logTrace("exeCount:${exes.size}")

    exes.forEach { exe ->
      val runTask = (exe as Executable).runTask!!
      val ldPath = exe.xtrasLibraryPath()
      logDebug("configureProjectTasks: exe:${exe.name} target:${exe.target.konanTarget.kotlinTargetName} ldPath:$ldPath")
      runTask.environment[HostManager.host.envLibraryPathName] = ldPath
    }

    val androidLibs =
      kotlinBinaries { it is SharedLibrary && it.target.konanTarget.family == Family.ANDROID }
    logTrace("androidLib count: ${androidLibs.size}")

    androidLibs.forEach { lib ->
      val copyTaskName =
        Xtras.Constants.TaskNames.copyAndroidLibsToJniFolderTaskName(lib)
      tasks.register<Copy>(copyTaskName) {
        val sharedLibDir = lib.linkTask.destinationDirectory.get().asFile
        dependsOn(lib.linkTask)
        prepareJniTask.dependsOn(name)
        from(sharedLibDir)
        into(lib.jniLibsDir)
        doLast {
          logInfo("$name: copied files from $sharedLibDir to ${lib.jniLibsDir}")
        }
      }
    }

    val libs = xtras.libraries.get()
    logTrace("libCount: ${libs.size}")
    xtras.nativeTargets.get().filter { it.family == Family.ANDROID }.forEach { androidTarget ->
      libs.forEach { lib ->
        val copyTaskName =
          Xtras.Constants.TaskNames.copyXtrasLibsToJniFolderTaskName(lib, androidTarget)
        tasks.register<Copy>(copyTaskName) {
          prepareJniTask.dependsOn(copyTaskName)
          dependsOn(PackageTaskName.EXTRACT.taskName(lib, androidTarget))
          val srcDir = lib.libsDir.invoke(androidTarget).resolve("lib")
          from(srcDir)
          val destDir =
            project.file("src").resolveAll("main", "jniLibs", androidTarget.androidLibDir!!)
          into(destDir)
          doLast {
            logInfo("$name: copied files from $srcDir to $destDir")
          }
        }
      }
    }

  }
}
