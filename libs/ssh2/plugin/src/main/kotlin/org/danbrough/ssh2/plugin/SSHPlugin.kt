package org.danbrough.ssh2.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xtrasRegisterLibrary


fun Project.ssh2(ssl: XtrasLibrary, extnName: String = "ssh2", block: XtrasLibrary.() -> Unit) =
  registerXtrasLibrary<XtrasLibrary>(extnName) {
    cinterops {
      declaration = """
        headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
        linkerOpts = -lssh2
    """.trimIndent()
    }
  }

class SSHPlugin : Plugin<Project> {
  override fun apply(target: Project) {
  }
}