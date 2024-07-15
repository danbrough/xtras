@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("SpellCheckingInspection")


import org.danbrough.xtras.core.openssl
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.sonatype.xtrasSonatype
import org.danbrough.xtras.xtras
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("com.android.library")
  `maven-publish`
}

buildscript {
  dependencies {
    //noinspection UseTomlInstead
    classpath("org.danbrough.xtras:core")
  }
}

group = projectProperty<String>("openssl.group")
version = projectProperty<String>("openssl.version")

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

  if (HostManager.hostIsLinux) {
    mingwX64()
    linuxX64()
    linuxArm64()
    androidNativeArm64()
    androidNativeX64()
  } else if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
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
        implementation(project(":libs:support")) //or implementation(project(":libs:support"))
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

xtrasSonatype {
}

xtrasAndroidConfig {

}

xtras.androidConfig {
  ndkApiVersion = 24
  minSDKVersion = 24
  compileSDKVersion = 24
}

val ssl = openssl {
  //buildEnabled = true

}

tasks.register("printSSL") {
  doFirst {
    println("${project.name}: buildEnabled: ${ssl.buildEnabled}")
  }
}