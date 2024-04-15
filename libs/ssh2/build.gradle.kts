@file:OptIn(ExperimentalKotlinGradlePluginApi::class)


import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.xtrasEnableTestExes
import org.danbrough.xtras.xtrasJniConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
}

//group = projectProperty<String>("ssh2.group")
//version = projectProperty<String>("ssh2.version")

xtras {
  kotlinApiVersion = KotlinVersion.KOTLIN_2_0
  kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0
  jvmTarget = JvmTarget.JVM_17
  javaVersion = JavaVersion.VERSION_17
}

group = projectProperty<String>("ssh2.group")
version = projectProperty<String>("ssh2.version")


kotlin {
  withSourcesJar(publish = true)

  compilerOptions {
    freeCompilerArgs = listOf("-Xexpect-actual-classes")
    languageVersion = xtras.kotlinLanguageVersion
    apiVersion = xtras.kotlinApiVersion
  }

  applyDefaultHierarchyTemplate()

  jvm()

  androidTarget {
  }

  linuxX64()
  linuxArm64()
  mingwX64()
  androidNativeArm64()

  sourceSets {
    all {
      languageSettings {
        listOf(
          "kotlin.ExperimentalStdlibApi",
          "kotlin.io.encoding.ExperimentalEncodingApi",
          "kotlin.experimental.ExperimentalNativeApi",
          "kotlinx.cinterop.ExperimentalForeignApi",
        ).forEach(::optIn)
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(libs.xtras.support) //or implementation(project(":libs:support"))
        implementation(libs.kotlinx.coroutines)
        //implementation(libs.kotlinx.io)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    val jniMain by creating {
      dependsOn(commonMain)
    }

    jvmMain {
      dependsOn(jniMain)
    }

    jvmTest {
      dependencies {
        implementation(kotlin("stdlib"))
      }
    }

    androidMain {
      dependsOn(jniMain)
    }
  }

  targets.withType<KotlinNativeTarget> {

    binaries {
      sharedLib("xtras_ssh2")
//      if (this@withType.konanTarget == KonanTarget.LINUX_X64) {
//        executable("execTest") {
//          entryPoint = "org.danbrough.ssh2.mainExecTest"
//          org.danbrough.ssh2.mainExecTest
//          compilation = compilations["test"]
//        }
//      }
    }

  }
}

xtrasEnableTestExes("ssh", tests = listOf("execTest")) {
  it in setOf(KonanTarget.LINUX_X64, KonanTarget.MINGW_X64)
}


xtrasTesting {

}

sonatype {
}

xtrasJniConfig {
  compileSdk = 34
}

registerXtrasGitLibrary<XtrasLibrary>("ssh2") {
  cinterops {
    declaration = """
    #headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
    #linkerOpts = -lssh2
    
    """.trimIndent()

    codeFile = project.file("test.h")
  }
}


//registerXtrasGitLibrary<LibraryExtension>()
/*

fun Project.ssh2(
  ssl: LibraryExtension,
  block: LibraryExtension.() -> Unit
) = registerGitLibrary<LibraryExtension>("ssh2") {
  dependsOn(ssl)

  cinterops {
    headers = """
      headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
      linkerOpts = -lssh2
      """.trimIndent()
  }

  prepareSource { target ->
    val configureFile = sourceDir(target).resolve("configure")
    outputs.file(configureFile)
    commandLine("autoreconf", "-fi")
    onlyIf {
      !configureFile.exists()
    }
  }

  configureSource(dependsOn = SourceTaskName.PREPARE) { target ->

    outputs.file(workingDir.resolve("Makefile"))

    doFirst {
      project.logWarn("RUNNING CONFIGURE WITH ${commandLine.joinToString(" ")}")
    }

    val args = mutableListOf(
      "./configure",
      "--with-libssl-prefix=${ssl.libsDir(target).absolutePath}",
      //"--enable-examples-build",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target).absolutePath}",
      "--with-libz"
    )

    commandLine(args)
  }

  compileSource { target ->
    val buildDir = buildDir(target)
    outputs.dir(buildDir)
    commandLine("make")
    doLast {
      buildDir.resolve("share").deleteRecursively()
    }
  }

  installSource {
    commandLine("make", "install")
  }


  block()
}


 */
