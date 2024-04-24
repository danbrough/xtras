package org.danbrough.testing

import org.danbrough.logging.log

import kotlin.test.Test

class NativeTests {
  @Test
  fun test1() {
    log.info { "test1()" }
    println("NativeTests:: test1()")

  }
}