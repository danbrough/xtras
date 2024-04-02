package org.danbrough.mqtt.subscribe

import kotlinx.cinterop.*
import org.danbrough.mqtt.AsyncContext
import org.danbrough.mqtt.Demo
import org.danbrough.mqtt.cinterops.*
import platform.posix.*
import org.danbrough.mqtt.log

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {

  printf("\u001b[1;32m### %s\u001b[0m\n","Hello there")
  println("\u001b[1;34m### Hello There again!\u001b[0m\n")

  log.info{"subscribe.main()!"}

  memScoped {
    
    val asyncContext = AsyncContext(alloc())

    runCatching {

      log.debug{"MQTTAsync_create"}
      MQTTAsync_create(
        asyncContext.client.ptr,
        Demo.address,
        Demo.clientID,
        MQTTCLIENT_PERSISTENCE_NONE,
        null
      ).also {
        if (it != MQTTASYNC_SUCCESS) error("MQTTAsync_create failed: %it")
      }

      log.debug{"MQTTAsync_setCallbacks"}
      MQTTAsync_setCallbacks(
        asyncContext.client.value,
        asyncContext.client.value,
        AsyncContext.onConnectionLost,
        AsyncContext.onMessageArrived,
        null
      ).also {
        if (it != MQTTASYNC_SUCCESS) error{"Failed to setCallbacks: $it"}
      }

//      val sslOptions = sslOptionsAsync().copy {
//        /*
//        			ssl_opts.verify = 1;
//		ssl_opts.CApath = opts.capath;
//		ssl_opts.keyStore = opts.cert;
//		ssl_opts.trustStore = opts.cafile;
//		ssl_opts.privateKey = opts.key;
//		ssl_opts.privateKeyPassword = opts.keypass;
//		ssl_opts.enabledCipherSuites = opts.ciphers;
//		conn_opts.ssl = &ssl_opts;
//         */
//        verify = 1
//        CApath = "/etc/ssl/certs".cstr.ptr
//
//      }


//      val useSSL =
//        Demo.address.startsWith("mqtts:") || Demo.address.startsWith("ssl:") || Demo.address.startsWith(
//          "wss:"
//        )
//
      val sslOpts = sslOptions().copy {
        verify = 1

        if (Demo.caPath.isNotBlank())
          CApath = Demo.caPath.cstr.ptr
        Demo.caFile?.also {
          trustStore = it.cstr.ptr
        }

      }

      log.info{"DEMO: $Demo"}
      val connOpts = connectOptionsAsync().copy {
        context = asyncContext.stableRef.asCPointer()
        keepAliveInterval = 20
        cleansession = 1
        if (Demo.username != null)
          username = Demo.username.cstr.ptr
        if (Demo.password != null)
          password = Demo.password.cstr.ptr
        onSuccess = AsyncContext.onConnect
        onFailure = AsyncContext.onConnectFailure
        //if (useSSL)
        ssl = sslOpts.ptr
      }

      connOpts.ptr.pointed.also { opts ->
        log.info{"username is ${opts.username?.toKString()}"}
        opts.connectProperties?.pointed?.also {
          log.debug{"connect properties: $it"}
        }
      }

      log.debug{"MQTTAsync_connect: ${Demo.address}"}
      MQTTAsync_connect(asyncContext.client.value, connOpts).also {
        if (it != MQTTASYNC_SUCCESS) error("Failed to start connect: $it")
      }

      log.debug{"finished calling connect"}
      sleep(1u)

      do {
        printf("Enter Q to quit:")
        fflush(stdout)
        val c = getchar()
        printf("\n")
        fflush(stdout)
      } while (c != 'Q'.code && c != 'q'.code)

    }.exceptionOrNull().also {

      if (it != null) log.error(it){it.message}
      log.info{"MQTTAsync_destroy"}
      MQTTAsync_destroy(asyncContext.client.ptr)
      log.info{"MQTTAsync_destroy: complete"}
    }
  }

}
