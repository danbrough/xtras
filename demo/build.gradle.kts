import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  `maven-publish`
  alias(libs.plugins.xtras)
}


repositories {
  maven("https://maven.danbrough.org")
  //maven("https://s01.oss.sonatype.org/content/groups/staging")
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
    compilations["main"].apply {
      defaultSourceSet {
      }
    }

    binaries {
      executable("demo") {
        entryPoint("demo.main")
      }
    }
  }
}



