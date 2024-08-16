@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasExtension
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
}




/*
java {
  sourceCompatibility = JavaConfig.javaVersion
  targetCompatibility = JavaConfig.javaVersion
}*/


kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()

/*  compilerOptions {
    languageVersion = JavaConfig.kotlinLanguageVersion
    apiVersion = JavaConfig.kotlinApiVersion
  }*/

  linuxX64()
  linuxArm64()
  mingwX64()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
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
}


xtrasAndroidConfig { }

xtrasTesting { }

