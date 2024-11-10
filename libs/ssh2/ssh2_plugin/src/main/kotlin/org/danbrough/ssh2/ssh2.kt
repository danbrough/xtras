package org.danbrough.ssh2

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.registerXtrasGitLibrary
import org.gradle.api.Project

fun Project.ssh2(ssl: XtrasLibrary, extnName: String = "ssh2", block: XtrasLibrary.() -> Unit) =
  registerXtrasGitLibrary<XtrasLibrary>(extnName) {
    cinterops {
      declaration = """
        headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
        linkerOpts = -lssh2
    """.trimIndent()
    }
  }
