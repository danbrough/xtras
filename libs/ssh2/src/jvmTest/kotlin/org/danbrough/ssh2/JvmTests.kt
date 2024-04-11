package org.danbrough.ssh2

import java.io.FileWriter
import kotlin.test.Test

class JvmTests {
  @Test
  fun testSshExec() {
    initSessionConfig(emptyArray())
    sshExec()
  }
}