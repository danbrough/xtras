@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.runningInIDE
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
}


xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}

object JavaConfig {
  val javaVersion = JavaVersion.VERSION_1_8
  val jvmTarget = JvmTarget.JVM_1_8
}


java {
  sourceCompatibility = JavaConfig.javaVersion
  targetCompatibility = JavaConfig.javaVersion
}


kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()

  compilerOptions {
    languageVersion.set(KotlinVersion.KOTLIN_1_9)
    apiVersion.set(KotlinVersion.DEFAULT)
  }


  declareSupportedTargets()


  jvm {
/*    compilerOptions {
      jvmTarget = JavaConfig.jvmTarget
    }*/
  }

  androidTarget {
    /*compilerOptions {
      jvmTarget = JavaConfig.jvmTarget
    }*/
  }


  sourceSets {
    all {
      languageSettings {
        listOf(
          "kotlinx.cinterop.ExperimentalForeignApi",
          "kotlin.io.encoding.ExperimentalEncodingApi",
        ).forEach(::optIn)
      }
    }

    val commonMain by getting {
      dependencies {
        api(libs.kotlin.logging)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    jvmMain {
      dependencies {
        api(libs.slf4j.api)
        api(libs.logback.classic)
      }
    }

    androidMain {
      dependencies {
        api(libs.slf4j.api)
        api(libs.slf4j.android)
      }
    }
  }


  targets.withType<KotlinNativeTarget> {
    if (konanTarget.family != Family.ANDROID) {
      compilations["main"].cinterops {
        create("jni") {
          defFile(project.file("src/cinterops/jni.def"))
          packageName = "platform.android"
          compilerOpts.add("-I${project.file("src/headers")}")
          when (konanTarget.family) {
            Family.LINUX -> "linux"
            Family.MINGW -> "win32"
            else -> error("Unhandled target: $konanTarget")
          }.also {
            compilerOpts.add("-I${project.file("src/headers/$it")}")
          }
        }
      }
    }
  }
}

android {
  compileSdk = 34
  namespace = "$XTRAS_PACKAGE.support"

  defaultConfig {
    minSdk = 22
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaConfig.javaVersion
    targetCompatibility = JavaConfig.javaVersion
  }

}


xtrasTesting()

sonatype {

}


