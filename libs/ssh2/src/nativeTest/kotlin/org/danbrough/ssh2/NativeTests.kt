package org.danbrough.ssh2


import kotlin.test.Test

class NativeTests {
  @Test
  fun test() {
    log.info { "test() jni ok: ${platform.android.JNI_OK}" }



  }
}