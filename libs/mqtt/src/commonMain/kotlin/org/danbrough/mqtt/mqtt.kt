package org.danbrough.mqtt

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.pointed
import org.danbrough.mqtt.cinterops.MQTTAsync_connectOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_message
import org.danbrough.mqtt.cinterops.mqtt_createConnectOptions
import org.danbrough.mqtt.cinterops.mqtt_createMessage

fun MemScope.createMQTTAsyncMessage(): MQTTAsync_message = mqtt_createMessage().getPointer(this).pointed

fun MemScope.createMQTTAsyncConnectOptions(): CPointer<MQTTAsync_connectOptions> =
  mqtt_createConnectOptions().getPointer(this)