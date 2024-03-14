package org.danbrough.xtras.support

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

expect fun initLogging(log: KLogger)

val supportLog = KotlinLogging.logger("XTRAS_SUPPORT").also {
  initLogging(it)
}