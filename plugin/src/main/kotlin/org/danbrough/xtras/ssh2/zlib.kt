package org.danbrough.xtras.ssh2

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.registerGitLibrary
import org.danbrough.xtras.tasks.SourceTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.danbrough.xtras.tasks.prepareSource
import org.gradle.api.Project


fun Project.ssh2(
  ssl: LibraryExtension,
  block: LibraryExtension.() -> Unit
) = registerGitLibrary<LibraryExtension>("ssh2") {
  dependsOn(ssl)

  cinterops {
    headers = """
      headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
      linkerOpts = -lssh2
      """.trimIndent()
  }

  prepareSource { target ->
    val configureFile = sourceDir(target).resolve("configure")
    outputs.file(configureFile)
    commandLine("autoreconf", "-fi")
    onlyIf {
      !configureFile.exists()
    }
  }

  configureSource(dependsOn = SourceTaskName.PREPARE) { target ->

    outputs.file(workingDir.resolve("Makefile"))

    doFirst {
      project.logWarn("RUNNING CONFIGURE WITH ${commandLine.joinToString(" ")}")
    }

    val args = mutableListOf(
      "./configure",
      "--with-libssl-prefix=${ssl.libsDir(target).absolutePath}",
      //"--enable-examples-build",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target).absolutePath}",
      "--with-libz"
    )

    commandLine(args)
  }

  compileSource { target ->
    val buildDir = buildDir(target)
    outputs.dir(buildDir)
    commandLine("make")
    doLast {
      buildDir.resolve("share").deleteRecursively()
    }
  }

  installSource {
    commandLine("make", "install_sw")
  }


  block()
}
