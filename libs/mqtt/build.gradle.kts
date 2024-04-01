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
//private const val DEFAULT_ADDRESS = "tcp://mqtt.eclipseprojects.io:1883"
//private const val DEFAULT_TOPIC = "MQTT_Examples"
//private const val DEFAULT_CLIENT_ID = "ExampleClientPub"
//private const val DEFAULT_CAPATH = ""// "/etc/ssl/certs"
//private const val DEFAULT_QOS = "1"

val testEnv = mutableMapOf(
  "MQTT_ADDRESS" to "tcp://mqtt.eclipseprojects.io:1883",
  "MQTT_TOPIC" to "MQTT Examples",
  "MQTT_CLIENT_ID" to "MQTTExampleClient",
  "MQTT_TOPIC" to "MQTT Examples",
  "MQTT_CAPATH" to "/etc/ssl/certs",
  "MQTT_QOS" to "1",
)

println("testEnv $testEnv")

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
        runTask?.environment?.putAll(testEnv)
        entryPoint = "org.danbrough.mqtt.publish.main"
      }

      executable("mqttSubscribe") {
        runTask?.environment?.putAll(testEnv)
        entryPoint = "org.danbrough.mqtt.subscribe.main"
      }
    }
  }
}



xtrasTesting()

sonatype {

}

