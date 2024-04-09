package org.danbrough.ssh2.tests

import org.danbrough.ssh2.SSH
import org.danbrough.ssh2.log
import org.danbrough.ssh2.testSessionConfig


fun mainSshExec() {
  log.info { "mainSshExec()" }
  log.debug { "config: $testSessionConfig" }

  val ssh = SSH()
  var session:SSH.Session? = null

  runCatching {
    ssh.initialize()
    session = ssh.connect(testSessionConfig)

  }.exceptionOrNull().also {
    if (it != null) log.error(it) { it.message }
    ssh.dispose()
  }
}


