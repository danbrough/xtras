package org.danbrough.mqtt

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.pointed
import org.danbrough.mqtt.cinterops.MQTTAsync_connectOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_message
import org.danbrough.mqtt.cinterops.mqtt_createConnectOptions
import org.danbrough.mqtt.cinterops.mqtt_createMessage
import org.danbrough.xtras.support.initLogging

val log = KotlinLogging.logger("MQTT").also {
  initLogging(it)
}

fun MemScope.createMQTTAsyncMessage(): MQTTAsync_message = mqtt_createMessage().getPointer(this).pointed

fun MemScope.createMQTTAsyncConnectOptions(): CPointer<MQTTAsync_connectOptions> =
  mqtt_createConnectOptions().getPointer(this)