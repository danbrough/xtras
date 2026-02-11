@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.xWarn
import org.danbrough.xtras.xtrasPublishing
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  //id("org.danbrough.openssl")
  id("org.danbrough.curl")
}

group = "org.danbrough.curl"

kotlin {
  linuxX64()
  linuxArm64()
  //androidNativeArm64()
  // androidNativeX64()
  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }
}

xtras {
  android {
    sdkVersion = 24
  }
}

xtrasTesting {
}

curl {
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


afterEvaluate {
  tasks.getByName("xtrasCurlGenerateCinterops").inputs.file("plugin/src/main/kotlin/org/danbrough/curl/plugin/curl.kt")
}

xtrasPublishing()