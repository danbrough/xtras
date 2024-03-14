import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.xtrasDocsDir
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  `maven-publish`
  alias(libs.plugins.xtras)
}

repositories {
  mavenCentral()
}

group = "$XTRAS_PACKAGE.demo"
version = "0.0.1-beta01"

kotlin {
  declareHostTarget()
  applyDefaultHierarchyTemplate()

  val commonMain by sourceSets.getting {

    dependencies {
      implementation("org.danbrough.openssl:openssl:0.0.1-alpha02")
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



