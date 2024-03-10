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

    code = """
       #include<libssh2.h>
   
     static int waitsocket(libssh2_socket_t socket_fd, LIBSSH2_SESSION *session)
     {
         struct timeval timeout;
         int rc;
         fd_set fd;
         fd_set *writefd = NULL;
         fd_set *readfd = NULL;
         int dir;

         timeout.tv_sec = 10;
         timeout.tv_usec = 0;

         FD_ZERO(&fd);

         FD_SET(socket_fd, &fd);

         /* now make sure we wait in the correct direction */
         dir = libssh2_session_block_directions(session);

         if(dir & LIBSSH2_SESSION_BLOCK_INBOUND)
             readfd = &fd;

         if(dir & LIBSSH2_SESSION_BLOCK_OUTBOUND)
             writefd = &fd;

         rc = select((int)(socket_fd + 1), readfd, writefd, NULL, &timeout);

         return rc;
     }
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
