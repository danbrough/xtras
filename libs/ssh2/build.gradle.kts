@file:OptIn(ExperimentalKotlinGradlePluginApi::class)


import org.danbrough.xtras.XtrasEnvironment
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.konanDir
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.tasks.SourceTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.danbrough.xtras.tasks.prepareSource
import org.danbrough.xtras.xtrasCommandLine
import org.danbrough.xtras.xtrasEnableTestExes
import org.danbrough.xtras.xtrasJniConfig
import org.danbrough.xtras.xtrasMsysDir
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
}

xtras {
  kotlinApiVersion = KotlinVersion.KOTLIN_2_0
  kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0
  jvmTarget = JvmTarget.JVM_17
  javaVersion = JavaVersion.VERSION_17
  cleanEnvironment = true
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
  //androidNativeArm64()

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

xtrasEnableTestExes("ssh", group.toString(), tests = listOf("sshExec")) {
  it in setOf(KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64, KonanTarget.MINGW_X64)
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
    headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
    linkerOpts = -lssh2
    
    """.trimIndent()

    codeFile = project.file("interops.h")
  }



  environment { target ->
    put("MAKEFLAGS", "-j6")

    if (target == KonanTarget.LINUX_ARM64 || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
      environmentKonan(this@registerXtrasGitLibrary,target)
    }

    //put("CC","clang")

  }

  prepareSource { target ->
    val args = if (HostManager.hostIsMingw)
      listOf("sh", project.xtrasMsysDir.resolveAll("usr", "bin", "autoreconf"), "-fi")
    else listOf("autoreconf", "-fi")
    xtrasCommandLine(args)
    outputs.file(workingDir.resolve("configure"))
  }

  configureSource(dependsOn = SourceTaskName.PREPARE) { target ->
    outputs.file(workingDir.resolve("Makefile"))

    val args = mutableListOf(
      "sh",
      "./configure",
      //"--enable-examples-build",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target).mixedPath}",
      "--with-libz"
    )

    if (target == KonanTarget.LINUX_ARM64)
      args += "--with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/3.3.0/linuxArm64" //TODO fix this

    xtrasCommandLine(args)
  }

  compileSource { target ->
    xtrasCommandLine("make")
  }

  installSource { target ->
    xtrasCommandLine("make", "install")

    doLast {
      copy {
        from(workingDir.resolve("example/.libs")) {
          include {
            @Suppress("UnstableApiUsage")
            !it.isDirectory && it.permissions.user.execute
          }
        }
        into(buildDir(target).resolve("bin"))
      }
    }
  }

}

