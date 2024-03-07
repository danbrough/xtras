package org.danbrough.iotest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging


internal expect fun init(log: KLogger)

val log = KotlinLogging.logger("TESTS").also {
  init(it)
}
