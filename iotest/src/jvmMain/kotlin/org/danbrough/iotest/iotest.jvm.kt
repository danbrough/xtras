package org.danbrough.iotest

import io.github.oshai.kotlinlogging.KLogger


internal actual fun init(log: KLogger) {
  log.info { "init()" }
}