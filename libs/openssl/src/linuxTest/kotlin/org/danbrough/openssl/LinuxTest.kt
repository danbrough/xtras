package org.danbrough.openssl

import klog.klogging
import kotlin.test.Test


class LinuxTest {
  val log = klogging.logger("DEMO")

  @Test
  fun test1() {
    log.trace { "test1(): trace" }
    log.debug { "test1(): trace" }
    log.info { "test1(): trace" }
    log.warn { "test1(): trace" }
    log.error { "test1(): trace" }
  }
}