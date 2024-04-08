@file:OptIn(ExperimentalForeignApi::class)

package org.danbrough.ssh2

import kotlinx.cinterop.ExperimentalForeignApi
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init

class SSH(flags: Int) {
  init {
    libssh2_init(flags).also {
      log.debug { "libssh2_init($flags) -> $it " }
    }
  }

  fun dispose(){
    log.trace { "libssh2_exit()" }
    libssh2_exit()
  }

}