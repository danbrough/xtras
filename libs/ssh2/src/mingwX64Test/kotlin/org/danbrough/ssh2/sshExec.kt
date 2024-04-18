package org.danbrough.ssh2

fun mainSshExec(args: Array<String>) {
  log.info { "mainSshExec()" }
  initSessionConfig(args)
  log.debug { "config: $sessionConfig" }

}

