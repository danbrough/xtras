package org.danbrough.examples

import kotlinx.cinterop.convert
import org.danbrough.ssh2.SessionConfig
import org.danbrough.xtras.support.getEnv


object TestConfig {
  private fun property(name: String, default: String): String =
    getEnv(name) ?: default

  val USER = property("SSH_USER", "dan")
  val HOSTNAME = property("SSH_HOST", "192.168.1.4")
  val PORT = property("SSH_PORT", "22").toUShort()
  val PUB_KEY = property("SSH_PUB_KEY", "/home/dan/.ssh/test.pub")
  val PRIVATE_KEY = property("SSH_PRIVATE_KEY", "/home/dan/.ssh/test")
  val PASSWORD = property("SSH_PASSWORD", "password")
  val COMMAND_LINE = property("SSH_COMMAND", "uptime")
  val knownHostsInput: String? ="/home/dan/.ssh/known_hosts"
  val knownHostsOutput: String? = "/home/dan/.ssh/known_hosts_dump"

  fun toSessionConfig() = SessionConfig( HOSTNAME,PORT.convert(),USER, PASSWORD, PUB_KEY, PRIVATE_KEY,
    knownHostsInput)
}
