package org.danbrough.xtras.core

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.tasks.SourceTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.danbrough.xtras.tasks.prepareSource
import org.danbrough.xtras.xtrasCommandLine
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasMsysDir
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

fun Project.ssh2(extnName: String = "ssh2", block: XtrasLibrary.() -> Unit) =
  registerXtrasGitLibrary<XtrasLibrary>(extnName) {

    cinterops {
      declaration = """
    headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
    linkerOpts = -lssh2 
    
    """.trimIndent()

      codeFile = project.file("interops.h")
    }

    environment { target ->
      put("MAKEFLAGS", "-j6")

      if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.MACOS_ARM64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
        environmentKonan(this@registerXtrasGitLibrary, target)
      } else if (target.family == Family.ANDROID) {
        environmentNDK(xtras, target)
      }

//put("CC","clang")
    }

    prepareSource {
      val args = if (HostManager.hostIsMingw)
        listOf("sh", project.xtrasMsysDir.resolveAll("usr", "bin", "autoreconf"), "-fi")
      else listOf("autoreconf", "-fi")
      xtrasCommandLine(args)
      outputs.file(workingDir.resolve("configure"))
    }

    configureSource(dependsOn = SourceTaskName.PREPARE) { target ->
      outputs.file(workingDir.resolve("Makefile"))

      val args = mutableListOf(
        "sh",
        "./configure"
      )


      args += "--host=${target.hostTriplet}"
      //args += "--target=${target.hostTriplet}"

      args += listOf(
        "--prefix=${buildDir(target).mixedPath}",
        "--with-libz"
      )

      args += "--with-libssl-prefix=${xtrasLibsDir}/openssl/${project.projectProperty<String>("openssl.version")}/${target.kotlinTargetName}" //TODO fix this
      xtrasCommandLine(args)
    }

    compileSource {
      xtrasCommandLine("make")
    }

    installSource { target ->
      xtrasCommandLine("make", "install")

      doLast {
        copy {
          from(workingDir.resolve("example/.libs")) {
            include {
              @Suppress("UnstableApiUsage")
              !it.isDirectory && it.permissions.user.execute
            }
          }
          into(buildDir(target).resolve("bin"))
        }
      }
    }

    block()
  }


