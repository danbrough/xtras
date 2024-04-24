package org.danbrough.logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

expect fun initLogging(log: KLogger)

val supportLog = KotlinLogging.logger("XTRAS_SUPPORT").also {
  initLogging(it)
}


expect fun getEnv(name: String): String?
