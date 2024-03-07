package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import org.danbrough.testing.initTesting
import kotlin.test.Test

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
}