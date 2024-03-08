package org.danbrough.xtras.ssh2

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.registerGitLibrary
import org.danbrough.xtras.tasks.SourceTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.prepareSource
import org.gradle.api.Project


abstract class LibSSH2Library(group: String, name: String, version: String, project: Project) :
  LibraryExtension(group, name, version, project)


fun Project.ssh2(
  ssl: LibraryExtension,
  block: LibSSH2Library.() -> Unit
) = registerGitLibrary<LibSSH2Library>("ssh2") {
  dependsOn(ssl)

  cinterops {
    headers = """
      package = $group.cinterops
      headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
      linkerOpts = -lssh2
      """.trimIndent()
  }

  prepareSource { target ->
    val configureFile = sourceDir(target).resolve("configure")
    outputs.file(configureFile)
    commandLine("autoreconf", "-fi")
    onlyIf {
      !configureFile.exists() && buildRequired.get().invoke(target)
    }
  }

  configureSource(dependsOn = SourceTaskName.PREPARE) { target ->
    val makeFile = workingDir.resolve("Makefile")
    outputs.file(makeFile)

    doFirst {
      project.logWarn("RUNNING CONFIGURE WITH ${commandLine.joinToString(" ")}")
    }
    onlyIf {
      !makeFile.exists() && buildRequired.get().invoke(target)
    }
    val args = mutableListOf(
      "./configure",
      "--with-libssl-prefix=${ssl.libsDir(target).absolutePath}",
      "--enable-examples-build",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}"
    )

    commandLine(args)
  }

  compileSource { target ->
    val buildDir = buildDir(target)
    outputs.dir(buildDir)
    doFirst {
      environment.keys.sorted().forEach {
        project.logWarn("ENV: $it: ${environment[it]}")
      }
    }
    commandLine("make", "install")
  }

  block()
}
