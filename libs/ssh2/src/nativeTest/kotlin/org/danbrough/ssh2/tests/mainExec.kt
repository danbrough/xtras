package org.danbrough.ssh2.tests

import org.danbrough.ssh2.SSH
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.log
import org.danbrough.ssh2.testSessionConfig
import org.danbrough.xtras.support.getEnv


fun mainExec() {
  log.info { "mainExec()" }
  log.debug { "config: $testSessionConfig" }

  val ssh = SSH()
  runCatching {
    ssh.initialize()
  }.exceptionOrNull().also {
    if (it != null) log.error(it) { it.message }
    ssh.dispose()
  }
}


