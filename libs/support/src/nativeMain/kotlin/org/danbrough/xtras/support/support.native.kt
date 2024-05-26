package org.danbrough.xtras.support

import kotlinx.cinterop.toKString
import platform.posix.getenv


actual fun getEnv(name: String):String? = getenv(name)?.toKString()