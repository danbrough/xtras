package org.danbrough.mqtt

import kotlinx.cinterop.toKString

actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()