package org.danbrough.ssh2

import java.io.FileWriter
import kotlin.test.Test

class JvmTests {
  @Test
  fun test1() {
    log.warn { "running test1()" }
    runCatching {
      log.info { "loading ssh2" }
      System.loadLibrary("ssh2")
      log.info { "loaded ssh2" }
      log.debug { "calling SSH2JNI.initSSH2()" }
      SSH2JNI.initSSH2().also {
        log.trace { "initSSH2 returned $it" }
      }
      log.info { "finished" }


    }.exceptionOrNull()?.also {
      log.error(it) { it.message }
    }
  }
}