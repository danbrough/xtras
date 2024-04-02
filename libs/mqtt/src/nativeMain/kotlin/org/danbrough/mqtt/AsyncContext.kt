package org.danbrough.mqtt

import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.copy
import kotlinx.cinterop.cstr
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import org.danbrough.mqtt.cinterops.MQTTASYNC_SUCCESS
import org.danbrough.mqtt.cinterops.MQTTAsyncVar
import org.danbrough.mqtt.cinterops.MQTTAsync_SSLOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_connect
import org.danbrough.mqtt.cinterops.MQTTAsync_connectOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_connectionLost
import org.danbrough.mqtt.cinterops.MQTTAsync_create
import org.danbrough.mqtt.cinterops.MQTTAsync_failureData
import org.danbrough.mqtt.cinterops.MQTTAsync_free
import org.danbrough.mqtt.cinterops.MQTTAsync_freeMessage
import org.danbrough.mqtt.cinterops.MQTTAsync_message
import org.danbrough.mqtt.cinterops.MQTTAsync_messageArrived
import org.danbrough.mqtt.cinterops.MQTTAsync_onFailure
import org.danbrough.mqtt.cinterops.MQTTAsync_onSuccess
import org.danbrough.mqtt.cinterops.MQTTAsync_setCallbacks
import org.danbrough.mqtt.cinterops.MQTTAsync_subscribe
import org.danbrough.mqtt.cinterops.MQTTAsync_successData
import org.danbrough.mqtt.cinterops.MQTTCLIENT_PERSISTENCE_NONE
import org.danbrough.mqtt.cinterops.callOptions
import org.danbrough.mqtt.cinterops.connectOptionsAsync
import org.danbrough.mqtt.cinterops.sslOptions
import platform.posix.aligned_alloc
import kotlin.native.ref.createCleaner

class AsyncContext(
  val address: String? = null,
  val clientID: String? = null,
  val username: String? = null,
  val password: String? = null,
) {

  val stableRef = StableRef.create(this)
  val client: MQTTAsyncVar = nativeHeap.alloc()


  fun destroy() {
    log.debug { "destroy()" }
    stableRef.dispose()
    nativeHeap.free(client)
  }

  companion object {

    private val COpaquePointer?.asyncContext: AsyncContext
      get() = this?.asStableRef<AsyncContext>()?.get() ?: error { "void* is null" }

    private val onMessageArrived: CPointer<MQTTAsync_messageArrived> =
      staticCFunction { ctx, topicName, topicLen, message ->
        ctx.asyncContext.onMessageArrived(topicName, topicLen, message)
        1
      }

    private val onConnect: CPointer<MQTTAsync_onSuccess> = staticCFunction { ctx, response ->
      ctx.asyncContext.onConnect(response)
    }

    private val onConnectionLost: CPointer<MQTTAsync_connectionLost> = staticCFunction { ctx, cause ->
      ctx.asyncContext.onConnectionLost(cause)
    }

    private val onConnectFailure: CPointer<MQTTAsync_onFailure> = staticCFunction { ctx, response ->
      ctx.asyncContext.onConnectFailure(response)
    }

    private val onDisconnectFailure: CPointer<MQTTAsync_onFailure> = staticCFunction { ctx, response ->
      ctx.asyncContext.onDisconnectFailure(response)
    }

    private val onDisconnect: CPointer<MQTTAsync_onSuccess> = staticCFunction { ctx, response ->
      ctx.asyncContext.onDisconnect(response)
    }

    private val onSubscribe: CPointer<MQTTAsync_onSuccess> = staticCFunction { ctx, response ->
      ctx.asyncContext.onSubscribe(response)
    }

    private val onSubscribeFailure: CPointer<MQTTAsync_onFailure> = staticCFunction { ctx, response ->
      ctx.asyncContext.onSubscribeFailure(response)
    }
  }

  fun create() {
    log.debug { "asyncContext.create()" }
    MQTTAsync_create(
      client.ptr,
      address,
      clientID,
      MQTTCLIENT_PERSISTENCE_NONE,
      null
    ).also {
      if (it != MQTTASYNC_SUCCESS) error("MQTTAsync_create failed: %it")
    }

    log.trace { "MQTTAsync_setCallbacks" }
    MQTTAsync_setCallbacks(
      client.value,
      client.value,
      onConnectionLost,
      onMessageArrived,
      null
    ).also {
      if (it != MQTTASYNC_SUCCESS) error { "MQTTAsync_setCallbacks failed: $it" }
    }
  }

  private fun onConnect(response: CPointer<MQTTAsync_successData>?) {
    log.info { "onConnect()" }

    val opts = callOptions().copy {
      onSuccess = onSubscribe
      onFailure = onSubscribeFailure
      context = stableRef.asCPointer()
    }

    log.info { "finished on connect. calling MQTTAsync_subscribe topic:${Demo.topic} qos: ${Demo.qos}" }

    MQTTAsync_subscribe(client.value, Demo.topic, Demo.qos, opts).also {
      if (it != MQTTASYNC_SUCCESS) {
        //finished = 1
        error("MQTTAsync_subscribe failed: $it")
      }
    }
  }

  private fun onConnectionLost(cause: CPointer<ByteVarOf<Byte>>?) {
    log.warn { "onConnectionLost: ${cause?.toKString()}" }

    val connOptions = connectOptionsAsync().copy {
      keepAliveInterval = 20
      cleansession = 1
      onSuccess = onConnect
      onFailure = onConnectFailure
    }
    log.info { "reconnecting..." }
    MQTTAsync_connect(client.value, connOptions).also {
      if (it != MQTTASYNC_SUCCESS) error { "Failed to reconnect: $it" }
    }
  }

  private fun onConnectFailure(response: CPointer<MQTTAsync_failureData>?) {
    log.trace { "onConnectFailure()" }

    //        finished = 1;

    response?.pointed?.also { failureData ->
      error { "onConnectFailure(): code:${failureData.code} message:${failureData.message?.toKString()}" }
    }
  }

  private fun onSubscribeFailure(response: CPointer<MQTTAsync_failureData>?) {
    log.trace { "onSubscribeFailure" }

    //        finished = 1;
    response?.pointed?.also { failureData ->
      log.info { "onSubscribeFailure(): code:${failureData.code} token:${failureData.token} message:${failureData.message?.toKString()}" }
    }

  }

  private fun onSubscribe(response: CPointer<MQTTAsync_successData>?) {
    log.trace { "onSubscribe" }

    ////        subscribed = 1;
    response?.pointed?.also { successData ->
      log.info { "onSubscribe(): token:${successData.token}" }
    }
  }

  private fun onDisconnect(response: CPointer<MQTTAsync_successData>?) {
    log.trace { "onDisconnect" }
    response?.pointed?.also { successData ->
      log.info { "onDisconnect(): token:${successData.token}" }
    }
  }

  private fun onDisconnectFailure(response: CPointer<MQTTAsync_failureData>?) {
    ////        disc_finished = 1;
    log.trace { "onDisconnectFailure" }
    response?.pointed?.also { failureData ->
      log.info { "onDisconnectFailure(): token:${failureData.token} message:${failureData.message?.toKString()}" }
    }
  }

  private fun onMessageArrived(
    topicName: CPointer<ByteVarOf<Byte>>?,
    topicLen: Int,
    message: CPointer<MQTTAsync_message>?
  ): Int {
    memScoped {

      log.trace { "onMessageArrived()" }
      val topic = topicName?.readBytes(topicLen)?.decodeToString()?.also {
        MQTTAsync_free(topicName)
      }

      log.trace { "topic <$topic>" }

      val msg = message?.pointed?.let {
        log.trace { "payload len: ${it.payloadlen}" }
        it.payload?.readBytes(it.payloadlen)?.decodeToString().also {
          log.error { "freeing message" }
          MQTTAsync_freeMessage(cValuesOf(message))
        }
      }

      log.trace { "onMessageArrived: finished: <$msg>" }

    }

    return 1
  }

  private var sslOpts:CValue<MQTTAsync_SSLOptions>? = null

  fun ssl(block:MQTTAsync_SSLOptions.()->Unit) {
    sslOpts?.also { error("ssl already configured") }
    sslOpts = sslOptions().copy {
      block()
    }
  }

  fun connect(block:MQTTAsync_connectOptions.()->Unit = {} ) {
    memScoped {
      val connOpts = connectOptionsAsync().copy {
        context = stableRef.asCPointer()
        keepAliveInterval = 20
        cleansession = 1
        if (Demo.username != null)
          username = Demo.username.cstr.ptr
        if (Demo.password != null)
          password = Demo.password.cstr.ptr
        onSuccess = onConnect
        onFailure = onConnectFailure
        ssl = sslOpts?.ptr
        block()
      }

      MQTTAsync_connect(client.value, connOpts).also {
        if (it != MQTTASYNC_SUCCESS) error("Failed to start connect: $it")
      }
    }
  }
}

