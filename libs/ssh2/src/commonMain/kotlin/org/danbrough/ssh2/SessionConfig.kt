package org.danbrough.ssh2

import org.danbrough.xtras.support.getEnv

data class SessionConfig(


  /**
   * IP address to connect to.
   * Defaults to "127.0.0.1"
   */
  val hostName:String = "127.0.0.1",

  /**
   * Port number to connect to.
   * Defaults to 22.
   */
  val port:Int = 22,

  /**
   * SSH username to connect to
   */
  val user: String? = null,

  /**
   * SSH password for password authentication or the passphrase for key authentication
   */
  val password:String? = null,

  /**
   * Path to the public key file
   */
  val publicKeyFile :String? = null,

  /**
   * Path to the private key file
   */
  val privateKeyFile :String? = null,

  /**
   * Where known hosts are stored.
   * Defaults to $HOME/.ssh/known_hosts
   */
  val knownHostsFile: String? = "${getEnv("HOME")}/.ssh/known_hosts"
)

