import org.danbrough.xtras.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("com.android.library")
}

group = "$XTRAS_PACKAGE.support"

xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}


kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  @OptIn(ExperimentalKotlinGradlePluginApi::class) compilerOptions {
    languageVersion.set(KotlinVersion.KOTLIN_1_8)

  }


  if (runningInIDE) {
    declareHostTarget()
  } else {
    declareSupportedTargets()
  }
  jvm()
  androidTarget()


  sourceSets {
    jvm {
      compilations.all {
        // kotlin compiler compatibility options
        kotlinOptions {
          jvmTarget = "1.8"
        }
      }
    }

    val commonMain by getting {
      dependencies {
        api(libs.kotlin.logging)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    val nativeMain by getting {}

    jvmMain {
      dependencies {
        api(libs.slf4j.api)
        implementation(libs.slf4j.simple)
      }
    }

    androidMain {
      dependencies {
        api(libs.slf4j.api)
        implementation(libs.slf4j.simple)
      }
    }
  }
}

android {
  compileSdk = 34
  namespace = "$XTRAS_PACKAGE.support"
  defaultConfig {
    minSdk = 22
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

}
/*
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}
*/

xtrasTesting()

sonatype {

}
