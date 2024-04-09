package org.danbrough.examples.ssh2

import org.danbrough.examples.TestConfig
import org.danbrough.ssh2.SSH

fun sshExec2() {
  log.info { "sshExec2()" }
  val ssh = SSH()
  var session:SSH.Session

  runCatching {
    ssh.initialize()
    session = ssh.connect(TestConfig.toSessionConfig())
  }.exceptionOrNull().also {
    if (it != null) log.error(it) { it.message }
    ssh.dispose()
  }
}