package org.danbrough.ssh2

import org.danbrough.xtras.support.getEnv


lateinit var sessionConfig: SessionConfig

fun initSessionConfig(args: Array<String>) {
  val props = mutableMapOf<String, String>()

  args.forEach {
    val (name, value) = it.split("=")
    props[name] = value
  }

  fun configProperty(name: String): String? =
    if (props.contains("ssh.$name"))
      props["ssh.$name"]
    else
      getEnv("SSH_${name.uppercase()}")

  sessionConfig = SessionConfig(
    configProperty("user") ?: getEnv("USER")!!,
    configProperty("hostname") ?: "127.0.0.1",
    configProperty("port")?.toInt() ?: 22,
    configProperty("password"),
    configProperty("public_key_file"),
    configProperty("private_key_file"),
    configProperty("known_hosts"),
    configProperty("auth_method")?.let { SessionConfig.AuthMethod.valueOf(it.uppercase()) },
  )
}