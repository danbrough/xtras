import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.xtrasDocsDir
import org.danbrough.xtras.xtrasMavenDir
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
  applyDefaultHierarchyTemplate()

  linuxX64()
  macosX64()
  macosArm64()


  sourceSets {
    commonMain {
      dependencies {
        //implementation("org.danbrough.xtras.openssl:openssl:0.0.1-alpha02")
        implementation("org.danbrough.xtras:support:0.0.1-beta01")
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



