@file:OptIn(ExperimentalForeignApi::class)

package org.danbrough.ssh2

import kotlinx.cinterop.ExperimentalForeignApi
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init
import org.danbrough.ssh2.cinterops.ssh2_exit
import org.danbrough.ssh2.cinterops.ssh2_init

class SSH {
  fun initialize(initFlags: Int = 0) {
    ssh2_init(initFlags).also {
      if (it != 0) error("ssh2_init() returned $it")
      else log.trace { "ssh2_init()" }
    }
  }

  fun dispose() {
    log.trace { "ssh2_exit()" }
    ssh2_exit()
  }


  class Session internal constructor(val config: SessionConfig){
    internal fun connect(){

    }
  }

  fun connect(sessionConfig: SessionConfig): Session {
    log.info { "SSH.connect() ${sessionConfig.user}@${sessionConfig.hostName}:${sessionConfig.port}" }
    return Session(sessionConfig).also(Session::connect)
  }
}