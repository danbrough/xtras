package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import org.danbrough.xtras.support.initLogging

val log = KotlinLogging.logger("SSH_EXEC").also {
  initLogging(it)
}

fun mainExecTest(args: Array<String>) {
  log.info { "mainExecTest()" }
  log.trace { "trace test" }
}