package org.danbrough.xtras.support

actual fun getEnv(name: String): String? = System.getenv(name)