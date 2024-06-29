@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.supportsJNI
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

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


sonatype {

}


