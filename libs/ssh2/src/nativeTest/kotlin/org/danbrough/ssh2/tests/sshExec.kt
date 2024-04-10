package org.danbrough.ssh2.tests

import org.danbrough.ssh2.SSH
import org.danbrough.ssh2.initSessionConfig
import org.danbrough.ssh2.log
import org.danbrough.ssh2.sessionConfig


fun mainSshExec(args: Array<String>) {
  log.info { "mainSshExec()" }
  initSessionConfig(args)
  log.debug { "config: $sessionConfig" }

  SSH().use { ssh ->
    runCatching {
      ssh.connect(sessionConfig).use { session ->
        session.openChannel().use { channel ->
          log.debug { "opened channel: $channel" }
          channel.exec("echo The date is `date`")
          channel.readLoop()
        }
      }
    }.exceptionOrNull().also { err ->
      if (err != null) log.error(err) { err.message }
    }
  }


}


