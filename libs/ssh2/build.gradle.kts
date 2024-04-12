@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.XtrasVersions
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.xtrasEnableTestExes
import org.danbrough.xtras.xtrasJniConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")

}

group = "org.danbrough.ssh2"
version = "0.0.1-alpha01"



java {
  sourceCompatibility = XtrasVersions.javaVersion
  targetCompatibility = XtrasVersions.javaVersion
}

xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


val ssh2 = ssh2(openssl()) {
  buildEnabled = true
}



kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  declareSupportedTargets()
  
  compilerOptions {
    freeCompilerArgs = listOf("-Xexpect-actual-classes")

    languageVersion = XtrasVersions.kotlinLanguageVersion
    apiVersion = XtrasVersions.kotlinApiVersion
  }

  jvm()

  androidTarget {
  }


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
        implementation(project(":libs:support"))
        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.io)
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
    }
  }
}


//val jvmRuntimeClasspath by configurations.existing

xtrasTesting()

sonatype {
}

xtrasEnableTestExes("ssh", tests = listOf("sshExec", "ioTest"))

xtrasJniConfig(javaVersion = XtrasVersions.javaVersion) {
  compileSdk = 34
}


