@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.logError
import org.danbrough.xtras.supportsJNI
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("com.android.library")
}

group = "org.danbrough"

kotlin {

  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()

  jvm()
  androidTarget()

//  if (HostManager.hostIsMac) {
  macosArm64()
  macosX64()
  // } else {
  linuxX64()
  linuxArm64()
  mingwX64()

  androidNativeArm64()
  androidNativeX64()
  androidNativeArm32()
  //}

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
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }


  targets.withType<KotlinNativeTarget> {
    if (konanTarget.supportsJNI) {
      logError("Creating interops for $konanTarget")
      /**
       * Generate the platform.android jni header bindings for targets that aren't android
       */
      compilations["main"].cinterops {
        create("jni") {
          packageName = "org.danbrough.jni.cinterops"
          logError("packageName: $packageName")
          val headersDir = project.file("src").resolve("headers")
          val osDir = when (konanTarget.family) {
            Family.LINUX -> "linux"
            Family.MINGW -> "win32"
            Family.IOS, Family.TVOS, Family.WATCHOS, Family.OSX -> "darwin"
            Family.ANDROID -> "android"
            else -> error("Unhandled target: $konanTarget")
          }.let { headersDir.resolve(it) }

          if (konanTarget.family == Family.ANDROID) {
            headers(osDir.resolve("jni.h"))
            includeDirs(osDir)
          } else {
            logError("HEADERS: ${headersDir.resolve("jni.h")} and ${osDir.resolve("jni_md.h")}")
            headers(headersDir.resolve("jni.h"), osDir.resolve("jni_md.h"))
            includeDirs(headersDir, osDir)
          }
        }
      }
    }
  }
}


xtrasAndroidConfig { }

xtrasTesting { }

