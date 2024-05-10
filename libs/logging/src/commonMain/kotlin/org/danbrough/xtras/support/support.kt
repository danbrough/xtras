package org.danbrough.xtras.support

import io.github.oshai.kotlinlogging.KLogger

expect fun initLogging(log: KLogger)


expect fun getEnv(name: String): String?
