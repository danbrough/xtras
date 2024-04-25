package org.danbrough.ssh2

fun sshExec() {
  log.info { "sshExec()" }

  val sshImpl = createSSH()

  /**
   * Must cast to AutoClosable as it's not actualized on JVM
   * @see https://youtrack.jetbrains.com/issue/KT-55777/Unresolved-kotlin.AutoCloseable-in-JVM
   */
  //sshImpl as AutoCloseable

  sshImpl.use { ssh ->
    log.error { "SSH: $ssh" }
    val config = sessionConfig

    log.debug { "config: $config" }
  }


}
