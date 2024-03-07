package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init
import org.danbrough.testing.initTesting
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.err
import platform.posix.getaddrinfo
import platform.posix.htons
import platform.posix.in_addr_t
import platform.posix.shutdown
import platform.posix.sockaddr
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.socklen_t
import platform.posix.strerror
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

val log = run {
  initTesting()
  KotlinLogging.logger("TESTS")
}

@OptIn(ExperimentalForeignApi::class)
class Tests {

  @Test
  fun test() {
    log.trace { "test() trace" }
    log.info { "test()" }
    log.error { "test() error" }
  }

  @Test
  fun testExec() {
    log.info { "testExec()" }

    val config = object {
      val user = "dan"
      val hostname = "192.168.1.4"
      val port = 22.toUShort()
    }

    runBlocking {
      var sock = 0

      launch {
        memScoped {

          val rc = libssh2_init(0);
          log.debug { "got rc: $rc" }
          if (rc != 0) {
            //fprintf(stderr, "libssh2 initialization failed ($rc)\n");
            error("libssh2 initialization failed ($rc)")
          }

          val hostaddr = inet_addr(config.hostname)

          sock = socket(AF_INET, SOCK_STREAM, 0)
          if (sock == LIBSSH2_INVALID_SOCKET) {
            error("failed to create socket!")
          }

          val sin = cValue<sockaddr_in>() {
            sin_family = AF_INET.toUShort()
            sin_port = htons(config.port)
            sin_addr.s_addr = hostaddr
          }

          connect(sock, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().toUInt()).also {
            log.trace { "connected returned $it" }
            if (it != 0)
              error("Failed to connect: ${strerror(it)?.toKString()}")
          }

          /*
              if(connect(sock, (struct sockaddr*)(&sin), sizeof(struct sockaddr_in))) {
        fprintf(stderr, "failed to connect!\n");
        goto shutdown;
    }
           */
          delay(1.seconds)
          log.debug { "exiting.." }
          libssh2_exit()
        }

    }.invokeOnCompletion {
      log.info { "finishing.." }


      if (sock != LIBSSH2_INVALID_SOCKET) {
        log.trace { "shutdown socket.." }
        shutdown(sock, 2)
        close(sock)
      }

      if (it != null)
        log.error(it) { "got an error: ${it.message}" }

      log.info { "exiting." }
    }
      }

  }
}