import org.danbrough.xtras.tasks.cinterops
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

openssl {
  cinterops {
  }
}

tasks.register("test") {
  doFirst {
    xWarn("version = ${project.kotlinExtension.coreLibrariesVersion}")
  }
}


