package org.danbrough.ssh2

fun sshExec() {
  log.info { "sshExec()" }

  /**
   * Must cast to AutoClosable as it's not actualized on JVM
   * @see https://youtrack.jetbrains.com/issue/KT-55777/Unresolved-kotlin.AutoCloseable-in-JVM
   */


  createSSH().use { ssh ->
    log.error { "SSH: $ssh" }
    val config = sessionConfig

    log.debug { "config: $config" }
  }


}
