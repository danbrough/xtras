import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.runningInIDE
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
}


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

  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  compilerOptions {
    languageVersion.set(KotlinVersion.KOTLIN_2_0)
    apiVersion.set(KotlinVersion.DEFAULT)
  }

  if (runningInIDE) {
    declareHostTarget()
  } else {
    declareSupportedTargets()
    macosX64()
  }
  jvm {
    compilations.all {
      // kotlin compiler compatibility options
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }

  androidTarget{
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions{
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }


  sourceSets {


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
        api(libs.logback.classic)
      }
    }

    androidMain {
      dependencies {
        api(libs.slf4j.api)
        api(libs.slf4j.android)
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


xtrasTesting()

sonatype {

}


