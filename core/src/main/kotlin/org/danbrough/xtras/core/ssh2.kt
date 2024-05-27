package org.danbrough.xtras.core

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.xtrasLibsDir
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


fun Project.ssh2(ssl: XtrasLibrary, extnName: String = "ssh2", block: XtrasLibrary.() -> Unit) =
  registerXtrasGitLibrary<XtrasLibrary>(extnName) {
    cinterops {
      declaration = """
        headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
        linkerOpts = -lssh2 
    """.trimIndent()
    }

    environment { target->
      if (target != null) {
        if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.MACOS_ARM64 || target == KonanTarget.LINUX_X64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
          environmentKonan(this@registerXtrasGitLibrary, target, this@ssh2)
        } else if (target.family == Family.ANDROID) {
          environmentNDK(xtras, target, this@ssh2)
          put("CC", "${get("PREFIX")}clang")
          put("CXX", "${get("PREFIX")}clang++")
        }
      }
    }

    buildCommand {target->
      val binDir = buildDir(target).resolve("bin")
      val copyExamples = if (target == KonanTarget.MINGW_X64)
        """cp example/*.exe ${binDir.absolutePath}""".trimIndent()
      else """
        mkdir ${binDir.absolutePath}
        cp example/.libs/* ${binDir.absolutePath}/
        """
      writer.println("""
        [ ! -f configure ] && autoreconf -fi 
        [ ! -f Makefile ] && ./configure --host=${target.hostTriplet} --prefix=${buildDir(target).mixedPath} \
        --with-libssl-prefix=${xtrasLibsDir}/openssl/${project.projectProperty<String>("openssl.version")}/${target.kotlinTargetName}       
        make
        make install
        $copyExamples
			""".trimIndent())
    }

    block()
  }


