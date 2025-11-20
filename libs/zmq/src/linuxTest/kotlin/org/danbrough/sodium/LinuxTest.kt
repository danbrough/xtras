package org.danbrough.sodium

import org.danbrough.sodium.cinterops.testFunction
import kotlin.test.Test

class LinuxTest {
  val log = klog.logger("DEMO")


  @Test
  fun test1() {
    println("HELLO")
    log.trace { "test1(): trace" }
    log.debug { "test1(): trace" }
    log.info { "test1(): trace" }
    log.warn { "test1(): trace" }
    log.error { "test1(): trace" }


    testFunction()


  }
}
