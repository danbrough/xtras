import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.androidLibDir
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.envLibraryPathName
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.targetNameMap
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
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
          "kotlinx.cinterop.ExperimentalForeignApi",
          "kotlin.io.encoding.ExperimentalEncodingApi",
          "kotlin.experimental.ExperimentalNativeApi",
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
      sharedLib("ssh2") {
        this as SharedLibrary
      }
    }
  }
}



android {
  compileSdk = 34
  namespace = "$XTRAS_PACKAGE.ssh2"

  defaultConfig {
    minSdk = 22
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaConfig.javaVersion
    targetCompatibility = JavaConfig.javaVersion
  }

  sourceSets["debug"].jniLibs {
    srcDir(project.file("src/jniLibs/debug"))
  }

  sourceSets["release"].jniLibs {
    srcDir(project.file("src/jniLibs/release"))
  }


}

xtrasTesting()

sonatype {
}

tasks.withType<KotlinNativeLink> {
  val konanTarget = binary.target.konanTarget

  if (konanTarget.family == Family.ANDROID) {
    val libsDir = outputs.files.files.first()
    val jniLibsDir =
      file("src/jniLibs/${if (binary.buildType.debuggable) "debug" else "release"}/${konanTarget.androidLibDir}")
    val taskCopyName = "${name}_copyLibs"
    tasks.register<Copy>(taskCopyName) {
      doFirst {
        logDebug("copying files from $libsDir to $jniLibsDir for ${this@withType.name}")
      }
      from(libsDir)
      into(jniLibsDir)
    }
    finalizedBy(taskCopyName)
  }
}

afterEvaluate {
  tasks.withType<KotlinJvmTest> {
    println("JVM TEST: $name")
    val linkTask = tasks.first {
      it is KotlinNativeLink &&
          it.binary is SharedLibrary &&
          it.binary.target.konanTarget == HostManager.host
          && it.binary.buildType == NativeBuildType.DEBUG
    }

    val env = environment[HostManager.host.envLibraryPathName]
    println("EXISTING ENV: $env")
    dependsOn(linkTask)
    environment(HostManager.host.envLibraryPathName, linkTask.outputs.files.files.first())
  }
}