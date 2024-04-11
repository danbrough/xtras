package org.danbrough.ssh2

import org.danbrough.ssh2.cinterops.ssh2_exit
import org.danbrough.ssh2.cinterops.ssh2_init

class SSHNative(initFlags:Int = 0) : SSH{
  init {
    ssh2_init(initFlags).also {
      if (it != 0) error("ssh2_init() returned $it")
      else log.trace { "ssh2_init()" }
    }
  }

  fun connect(sessionConfig: SessionConfig): SessionNative = SessionNative(sessionConfig).also(SessionNative::connect)
  override fun close() {
    log.trace { "SSH::close() .. calling ssh2_exit()" }
    ssh2_exit()
  }
}

actual fun createSSH(): SSH  = SSHNative()