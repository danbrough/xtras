@file:OptIn(ExperimentalKotlinGradlePluginApi::class)


import org.danbrough.xtras.core.openssl
import org.danbrough.xtras.core.sqlite
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.supportsJNI
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasTestExecutables
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
  //`maven-publish`
}

buildscript {
  dependencies {
    classpath(libs.xtras.core)
  }
}

xtras {
  kotlinApiVersion = KotlinVersion.KOTLIN_2_0
  kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0
  jvmTarget = JvmTarget.JVM_17
  javaVersion = JavaVersion.VERSION_17
}

group = projectProperty<String>("sqlite.group")
version = projectProperty<String>("sqlite.version")


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
  mingwX64()
  linuxArm64()
  androidNativeArm64()
  androidNativeX64()
  if (HostManager.hostIsMac) {
    macosArm64()
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
        implementation(project(":libs:support")) //or implementation(project(":libs:support"))
        implementation(libs.kotlinx.coroutines)
        //implementation(project(":libs:openssl"))
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
    if (konanTarget.supportsJNI)
      compilations["main"].defaultSourceSet.kotlin.srcDir(project.file("src").resolve("jni"))
    binaries {
      sharedLib("xtras_ssh2")
//        executable("sshExec") {
//          entryPoint = "org.danbrough.ssh2.mainSshExec"
//          compilation = compilations.getByName("test")
//        }
    }
  }
}



xtrasTesting {

}

sonatype {
}

xtrasAndroidConfig {
}

sqlite {

}

/*rootProject.findProject(":libs:openssl")!!.also {
  val openssl = it.extensions.getByType<XtrasLibrary>()
  logError("LIBS DIR OPENSSL: ${openssl.libsDir(KonanTarget.LINUX_ARM64)}")
}*/

