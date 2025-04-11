import org.danbrough.xtras.xWarn
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

openssl {}

tasks.register("test") {
  doFirst {
    xWarn("version = ${project.kotlinExtension.coreLibrariesVersion}")
  }
}


kotlin {
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

