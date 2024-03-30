package org.danbrough.mqtt


import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CValue
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import org.danbrough.mqtt.cinterops.MQTTAsyncVar
import org.danbrough.mqtt.cinterops.MQTTAsync_connectOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_message
import org.danbrough.mqtt.cinterops.createConnectOptions
import org.danbrough.mqtt.cinterops.createMessage
import org.danbrough.xtras.support.initLogging
import kotlin.test.Test

val log = KotlinLogging.logger("MQTT_TEST").also {
  initLogging(it)
}

class NativeTests {
  @Test
  fun test() {
    log.trace { "test()" }
    log.info { "running test" }
    memScoped {
      val client: MQTTAsyncVar = alloc()
      val connectOptions: CValue<MQTTAsync_connectOptions> = createConnectOptions()
      val message: CValue<MQTTAsync_message> = createMessage()
      log.trace { "that worked" }
    }
  }
}