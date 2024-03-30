import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.mqtt.mqtt
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
}

group = "org.danbrough.mqtt"
version = "0.0.1-alpha01"

xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


val mqtt = mqtt(openssl()) {
  buildEnabled = true
}



kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  declareSupportedTargets()

  sourceSets {
    all {
      languageSettings {
        listOf(
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

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val nativeMain by getting {
      dependencies {
        implementation(project(":libs:support"))
      }
    }
  }

  targets.withType<KotlinNativeTarget> {
    binaries {
      executable("mqttPublish") {
        entryPoint = "org.danbrough.mqtt.publish.main"
      }
    }
  }
}



xtrasTesting()

sonatype {

}

