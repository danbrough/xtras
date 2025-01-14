import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  //alias(libs.plugins.xtras)
}

kotlin {
  applyDefaultHierarchyTemplate()

  linuxX64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.klog.core)
        //implementation("org.danbrough.xtras.openssl:openssl:0.0.1-alpha02")
        //implementation("org.danbrough.xtras:support:0.0.1-beta01")
        implementation(project(":jni"))
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