package org.danbrough.ssh2

import kotlin.test.Test

class JvmTests {
  @Test
  fun testSshExec() {
    initSessionConfig(emptyArray())
    sshExec()
  }
}