@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("SpellCheckingInspection")


import org.danbrough.openssl.plugin.openssl
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasExtension
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("com.android.library")
  id("org.danbrough.openssl")
  `maven-publish`
}

val xtras = xtrasExtension

openssl {

}

kotlin {
  withSourcesJar(publish = true)

  compilerOptions {
    freeCompilerArgs = listOf("-Xexpect-actual-classes")

    languageVersion = xtras.kotlinLanguageVersion
    apiVersion = xtras.kotlinApiVersion
  }

  applyDefaultHierarchyTemplate()
  jvm()

  androidTarget {
  }


  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
    //iosX64()
    //iosArm64()
    //iosSimulatorArm64()
  } else {
    linuxArm64()
    linuxX64()
    mingwX64()
    androidNativeArm64()
    androidNativeX64()
  }

  sourceSets {
    all {
      languageSettings {
        listOf(
          "kotlin.ExperimentalStdlibApi",
          "kotlin.io.encoding.ExperimentalEncodingApi",
          "kotlin.experimental.ExperimentalNativeApi",
          "kotlinx.cinterop.ExperimentalForeignApi",
        ).forEach(::optIn)
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(project(":support")) //or implementation(project(":libs:support"))
        //implementation(libs.kotlinx.coroutines)
        //implementation(libs.kotlinx.io)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    val jniMain by creating {
      dependsOn(commonMain)
    }

    jvmMain {
      dependsOn(jniMain)
    }

    jvmTest {
      dependencies {
        implementation(kotlin("stdlib"))
      }
    }

    androidMain {
      dependsOn(jniMain)
    }
  }

  targets.withType<KotlinNativeTarget> {
    binaries {
      sharedLib("xtras_openssl")
    }
  }
}


xtrasTesting {
}

xtrasAndroidConfig {

}

xtras.androidConfig {
  ndkApiVersion = 24
  minSDKVersion = 24
  compileSDKVersion = 24
}

