@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.runningInIDE
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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

object JavaConfig {
  val javaVersion = JavaVersion.VERSION_1_8
  val jvmTarget = JvmTarget.JVM_1_8
}


java {
  sourceCompatibility = JavaConfig.javaVersion
  targetCompatibility = JavaConfig.javaVersion
}


kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()

  compilerOptions {
    languageVersion.set(KotlinVersion.KOTLIN_1_9)
    apiVersion.set(KotlinVersion.DEFAULT)
  }


  declareSupportedTargets()


  jvm {
/*    compilerOptions {
      jvmTarget = JavaConfig.jvmTarget
    }*/
  }

  androidTarget {
    /*compilerOptions {
      jvmTarget = JavaConfig.jvmTarget
    }*/
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

    val posixMain by creating {
      dependsOn(commonMain)
    }

    targets.withType<KotlinNativeTarget> {
/*      if (!konanTarget.family.isAppleFamily) {
        compilations["main"].defaultSourceSet.dependsOn(posixMain)
      }*/
    }


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
    sourceCompatibility = JavaConfig.javaVersion
    targetCompatibility = JavaConfig.javaVersion
  }

}


xtrasTesting()

sonatype {

}


