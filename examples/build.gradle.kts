import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
}

group = "org.danbrough.ssh2"
version = "0.0.1-alpha01"

xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


kotlin {
  applyDefaultHierarchyTemplate()
  declareHostTarget()


  sourceSets {
    all {
      languageSettings {
        listOf(
          "kotlinx.cinterop.ExperimentalForeignApi",
          "kotlin.io.encoding.ExperimentalEncodingApi",
        ).forEach(::optIn)
      }
    }

    commonMain {
      dependencies {
        implementation(project(":libs:support"))
        implementation(project(":libs:ssh2"))
      }
    }
  }

  targets.withType<KotlinNativeTarget> {
    binaries {
      executable("ssh2Exec", listOf(NativeBuildType.DEBUG)) {
        entryPoint = "org.danbrough.examples.ssh2.ssh2Exec"
      }
    }
  }
}