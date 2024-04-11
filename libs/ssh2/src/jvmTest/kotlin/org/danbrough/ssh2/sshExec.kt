package org.danbrough.ssh2

fun sshExec() {
  log.info { "sshExec()" }
  SSH().use { ssh ->
    log.error { "SSH: $ssh" }
    val config = sessionConfig

    log.debug { "config: $config" }
  }


}
