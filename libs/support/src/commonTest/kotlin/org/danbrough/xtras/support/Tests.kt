package org.danbrough.xtras.support

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.test.Test

val log = KotlinLogging.logger("TESTS")

class Tests {
  companion object{
    init {
      supportLog.trace { "" }
    }
  }
  @Test
  fun testLog(){
    log.trace { "testLog()" }
    log.debug { "testLog()" }
    log.info { "testLog()" }
    log.warn { "testLog()" }
    log.error { "testLog()" }
  }
}