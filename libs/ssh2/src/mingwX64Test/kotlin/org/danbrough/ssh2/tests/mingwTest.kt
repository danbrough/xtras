package org.danbrough.ssh2.tests

import org.danbrough.ssh2.initSessionConfig
import org.danbrough.ssh2.log
import platform.posix.connect
import platform.posix.shutdown
import platform.posix.socket


fun mainMingwTest(args: Array<String>) {
  log.info { "mainMingwTest()" }
  initSessionConfig(args)


}


