

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  `maven-publish`
  alias(libs.plugins.xtras)
}

repositories {
  //maven("https://maven.danbrough.org")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
  //maven(xtrasMavenDir)
}




kotlin {


  linuxX64()

  macosX64()
  macosArm64()


  sourceSets {
    commonMain {
      dependencies {

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



