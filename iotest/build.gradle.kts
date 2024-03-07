import org.danbrough.xtras.xtrasTesting

plugins {
  kotlin("multiplatform")
}


kotlin {
  applyDefaultHierarchyTemplate()

  linuxX64()
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.io)
        implementation(libs.kotlin.logging)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.slf4j.api)
        implementation(libs.logback.classic)

      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}

xtrasTesting()