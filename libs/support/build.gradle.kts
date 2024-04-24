@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

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

version = "0.0.1-beta01"

object JavaConfig {
  val javaVersion = JavaVersion.VERSION_1_8
  val jvmTarget = JvmTarget.JVM_1_8
  val kotlinLanguageVersion = KotlinVersion.KOTLIN_1_9
  val kotlinApiVersion = KotlinVersion.KOTLIN_1_9
}


java {
  sourceCompatibility = JavaConfig.javaVersion
  targetCompatibility = JavaConfig.javaVersion
}


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
  macosArm64()
  macosX64()

  androidNativeArm64()
  androidNativeX86()
  androidNativeX64()

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
            Family.IOS, Family.TVOS, Family.WATCHOS, Family.OSX -> "darwin"

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
  namespace = group.toString()

  defaultConfig {
    minSdk = 22
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaConfig.javaVersion
    targetCompatibility = JavaConfig.javaVersion
  }

}


xtrasTesting { }


sonatype {

}


