package org.danbrough.ssh2.tests

import org.danbrough.ssh2.SSH
import org.danbrough.ssh2.initSessionConfig
import org.danbrough.ssh2.log
import org.danbrough.ssh2.sessionConfig


fun mainSshExec(args:Array<String>) {
  log.info { "mainSshExec()" }
  initSessionConfig(args)
  log.debug { "config: $sessionConfig" }

  val ssh = SSH()
  var session:SSH.Session? = null

  runCatching {
    ssh.initialize()
    session = ssh.connect(sessionConfig)

  }.exceptionOrNull().also {
    if (it != null) log.error(it) { it.message }
    ssh.dispose()
  }
}


