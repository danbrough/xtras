@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.xWarn
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("org.danbrough.openssl")
}

group = "org.danbrough.openssl"

kotlin {
  linuxX64()
  linuxArm64()
  androidNativeArm64()
  androidNativeX64()
//  macosX64()
}

xtras {
  android {
    sdkVersion = 24
  }
}

xtrasTesting {
}

openssl {
}

tasks.register("test") {
  doFirst {
    xWarn("version = ${project.kotlinExtension.coreLibrariesVersion}")
  }
}


kotlin {
  compilerOptions {
    optIn = listOf("kotlinx.cinterop.ExperimentalForeignApi")
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.klog.core)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}

