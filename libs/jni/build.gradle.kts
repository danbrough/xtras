@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.supportsJNI
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("com.android.library")
}

object JavaConfig {
  val javaVersion = JavaVersion.VERSION_1_8
  val jvmTarget = JvmTarget.JVM_1_8
  val kotlinLanguageVersion = KotlinVersion.KOTLIN_1_9
  val kotlinApiVersion = KotlinVersion.KOTLIN_1_9
}

/*
java {
  sourceCompatibility = JavaConfig.javaVersion
  targetCompatibility = JavaConfig.javaVersion
}*/


kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()

  compilerOptions {
    languageVersion = JavaConfig.kotlinLanguageVersion
    apiVersion = JavaConfig.kotlinApiVersion
  }

  linuxX64()
  linuxArm64()
  mingwX64()
  if (HostManager.hostIsMac) {
    macosArm64()
    macosX64()
  }
  androidNativeArm64()
  androidNativeX64()
  androidNativeArm32()

  jvm()
  androidTarget()

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
    if (konanTarget.supportsJNI) {
      /**
       * Generate the platform.android jni header bindings for targets that aren't android
       */
      compilations["main"].cinterops {
        create("jni") {
          packageName = "${project.group}.jni"
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

