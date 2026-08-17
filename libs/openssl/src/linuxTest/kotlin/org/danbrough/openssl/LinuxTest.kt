package org.danbrough.openssl


import org.danbrough.openssl.cinterops.testFunction
import kotlin.test.Test

class LinuxTest {
  val log = org.danbrough.klog.logger("DEMO")


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