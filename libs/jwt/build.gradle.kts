@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("SpellCheckingInspection")


import org.danbrough.xtras.core.jwt
import org.danbrough.xtras.core.jansson
import org.danbrough.xtras.core.openssl
import org.danbrough.xtras.core.postgres
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
  `maven-publish`
}

buildscript {
  dependencies {
    classpath(libs.xtras.core)
  }
}



group = projectProperty<String>("jwt.group")
version = projectProperty<String>("jwt.version")

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

  linuxX64()
//  linuxArm64()
//  mingwX64()
//  androidNativeArm64()
//  androidNativeX64()
  if (HostManager.hostIsMac) {
    //macosArm64()
    macosX64()
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
        implementation(libs.xtras.support) //or implementation(project(":libs:support"))
        //implementation(libs.kotlinx.coroutines)
        //implementation(libs.kotlinx.io)
        implementation(libs.klog.core)
        implementation(libs.kotlinx.io)
        implementation(libs.kotlinx.datetime)
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
      sharedLib("xtras_jwt")
    }
  }
}

xtrasTesting {
}

sonatype {
}

xtrasAndroidConfig {
}

val sslLib = openssl {
  //buildEnabled = true
}
val janssonLib = jansson{

}
jwt(sslLib,janssonLib) {

}