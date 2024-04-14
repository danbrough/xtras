package org.danbrough.mqtt.subscribe

import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import org.danbrough.mqtt.AsyncContext
import org.danbrough.mqtt.Demo
import org.danbrough.mqtt.cinterops.MQTTAsync_destroy
import org.danbrough.mqtt.log
import platform.posix.fflush
import platform.posix.getchar
import platform.posix.printf
import platform.posix.sleep
import platform.posix.stdout


fun subscribeDemo() {

  printf("\u001b[1;32m### %s\u001b[0m\n", "Hello there")
  println("\u001b[1;34m### Hello There again!\u001b[0m\n")

  log.info { "subscribe.main()!" }



  memScoped {

    val asyncContext = AsyncContext(address = Demo.address, clientID = Demo.clientID)

    runCatching {

      asyncContext.create()


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
      asyncContext.ssl {
        CApath = Demo.caPath?.cstr?.ptr
        trustStore = Demo.caFile?.cstr?.ptr
      }

      asyncContext.connect()

      log.debug { "finished calling connect" }
      sleep(1u)

      do {
        printf("Enter Q to quit:")
        fflush(stdout)
        val c = getchar()
        printf("\n")
        fflush(stdout)
      } while (c != 'Q'.code && c != 'q'.code)

    }.exceptionOrNull().also {

      if (it != null) log.error(it) { it.message }
      log.info { "MQTTAsync_destroy" }
      MQTTAsync_destroy(asyncContext.client.ptr)
      log.info { "MQTTAsync_destroy: complete" }
      asyncContext.destroy()
    }
  }

}


@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
  printf("\u001b[1;32m### %s\u001b[0m\n", "Hello there")
  println("\u001b[1;34m### Hello There again!\u001b[0m\n")
  subscribeDemo()


}
