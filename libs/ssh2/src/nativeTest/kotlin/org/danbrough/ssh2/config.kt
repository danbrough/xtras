package org.danbrough.ssh2

import org.danbrough.xtras.support.getEnv

private fun testProperty(name: String) =
  getEnv("SSH_${name.uppercase()}")


val testSessionConfig by lazy {
  SessionConfig(
    testProperty("hostname") ?: "127.0.0.1",
    testProperty("port")?.toInt() ?: 22,
    testProperty("user"),
    testProperty("password"),
    testProperty("public_key_file"),
    testProperty("private_key_file"),
    testProperty("known_hosts"),
  )
}