package org.danbrough.xtras.support

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

internal expect fun initSupport(log: KLogger)

val supportLog = KotlinLogging.logger("XTRAS_SUPPORT").also {
  initSupport(it)
}