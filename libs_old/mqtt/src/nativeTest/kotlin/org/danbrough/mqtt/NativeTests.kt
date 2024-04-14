package org.danbrough.mqtt


import kotlinx.cinterop.memScoped
import kotlin.test.Test


class NativeTests {
  @Test
  fun test() {
    log.info { "running test" }
    memScoped {

    }
  }
}