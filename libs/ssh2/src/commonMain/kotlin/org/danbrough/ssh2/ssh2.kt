package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import org.danbrough.xtras.support.initLogging

internal val log = KotlinLogging.logger("SSH2").also {
  initLogging(it)
}

