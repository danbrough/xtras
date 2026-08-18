package org.danbrough.ssh2

import kotlinx.coroutines.runBlocking


fun mainSshExec(args: Array<String>) {
  log.info { "mainSshExec()" }
  initSessionConfig(args)
  log.debug { "config: $sessionConfig" }

  createSSH().use { ssh ->
    runCatching {
      ssh as SSHNative
      ssh.connect(sessionConfig).use { session ->
        runBlocking {

          session.openChannel().use { channel ->
            log.debug { "opened channel for cmd1" }
            channel.exec("ls /nowhere")
            channel.readLoop()
          }

          log.info { "opening second channel" }
          session.openChannel().use { channel ->
            log.debug { "opened channel for cmd2" }
            channel.exec("echo the date is `date`")
            channel.readLoop()
          }
        }
      }
    }.exceptionOrNull().also { err ->
      if (err != null) log.error(err) { err.message }
    }
  }
}


