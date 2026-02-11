import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
}


repositories {
  maven("https://maven.danbrough.org")
  google()
  mavenCentral()
}

kotlin {

  linuxX64()
  linuxArm64()
  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.xtras.support)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }


  targets.withType<KotlinNativeTarget> {
    binaries {
      executable("demo") {
        entryPoint("demo.main")
      }
    }
  }
}



