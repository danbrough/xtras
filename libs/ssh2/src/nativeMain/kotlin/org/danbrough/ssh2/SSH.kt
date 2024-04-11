package org.danbrough.ssh2

import org.danbrough.ssh2.cinterops.ssh2_exit
import org.danbrough.ssh2.cinterops.ssh2_init

class SSH(private val initFlags: Int = 0) : AutoCloseable {
  var counter = 0

  init {
    ssh2_init(initFlags).also {
      if (it != 0) error("ssh2_init() returned $it")
      else log.trace { "ssh2_init()" }
    }
  }

  override fun close() {
    log.trace { "SSH::close()" }
    ssh2_exit()
  }

  fun connect(sessionConfig: SessionConfig): Session = Session(sessionConfig).also(Session::connect)

  override fun toString() = "SSH[flags=$initFlags,counter:$counter]"

}
