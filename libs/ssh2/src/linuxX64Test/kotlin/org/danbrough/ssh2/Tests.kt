package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.danbrough.ssh2.interops.libssh2_exit
import org.danbrough.ssh2.interops.libssh2_init
import org.danbrough.testing.initTesting
import platform.linux.inet_addr
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

val log = run {
  initTesting()
  KotlinLogging.logger("TESTS")
}

class Tests {

  @Test
  fun test() {
    log.trace { "test() trace" }
    log.info { "test()" }
    log.error { "test() error" }
  }

  @Test
  fun testExec(){
    log.info { "testExec()" }

    val hostname = "rip.local"

    runBlocking {

      val rc = libssh2_init(0);
      log.debug { "got rc: $rc" }
      if(rc != 0) {
        //fprintf(stderr, "libssh2 initialization failed ($rc)\n");
        error("libssh2 initialization failed ($rc)")
      }

      inet_addr(hostname)
      delay(1.seconds)
      log.debug { "exiting.." }
      libssh2_exit()
    }


  }
}