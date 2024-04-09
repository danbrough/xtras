import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.androidLibDir
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.envLibraryPathName
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.targetNameMap
import org.danbrough.xtras.xtrasJniConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")

}

group = "org.danbrough.ssh2"
version = "0.0.1-alpha01"

object JavaConfig {
  val javaVersion = JavaVersion.VERSION_1_8
  val jvmTarget = JvmTarget.JVM_1_8
}


java {
  sourceCompatibility = JavaConfig.javaVersion
  targetCompatibility = JavaConfig.javaVersion
}

xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


val ssh2 = ssh2(openssl()) {
  buildEnabled = true
}



kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  declareSupportedTargets()

  jvm()
  androidTarget {
  }


  sourceSets {
    all {
      languageSettings {
        listOf(
          "kotlin.io.encoding.ExperimentalEncodingApi",
          "kotlin.experimental.ExperimentalNativeApi",
          "kotlinx.cinterop.ExperimentalForeignApi",
        ).forEach(::optIn)
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(project(":libs:support"))
        implementation(libs.kotlinx.coroutines)
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

    androidMain {
      dependsOn(jniMain)
    }
  }

  targets.withType<KotlinNativeTarget> {
    binaries {
      sharedLib("ssh2")

      listOf("sshExec").forEach { test ->
        executable(test, listOf(NativeBuildType.DEBUG)) {
          entryPoint = "org.danbrough.ssh2.tests.main${test.capitalized()}"
          compilation = compilations["test"]
          runTask?.apply {
            project.properties.forEach { (key, value) ->
              if (key.startsWith("ssh.")) {
                val envKey = key.replace('.','_').uppercase()
                println("SETTING $envKey to $value")
                environment(envKey, value!!)
              }
            }
          }
        }
      }

    }
  }
}




xtrasTesting()

sonatype {
}


xtrasJniConfig(javaVersion = JavaConfig.javaVersion) {
  compileSdk = 34
}

