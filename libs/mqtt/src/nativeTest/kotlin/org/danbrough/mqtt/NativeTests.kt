package org.danbrough.mqtt


import io.github.oshai.kotlinlogging.KotlinLogging
import org.danbrough.xtras.support.initLogging
import kotlin.test.Test

private val log = KotlinLogging.logger("XTRAS").also {
  initLogging(it)
}

class NativeTests {
  @Test
  fun test() {
    log.trace { "test()" }
    log.info { "running test" }

  }
}