package org.danbrough.xtras.core

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.registerXtrasGitLibrary
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


fun Project.sqlite(extnName: String = "sqlite", block: XtrasLibrary.() -> Unit) =
  registerXtrasGitLibrary<XtrasLibrary>(extnName) {
    cinterops {
      declaration = """
        headers = sqlite3ext.h  sqlite3.h
        linkerOpts = -lsqlite3
        
    """.trimIndent()
    }

    environment { target ->
      if (target != null) {
        if (target.family == Family.ANDROID) {
          environmentNDK(xtras, target, this@sqlite)
          put("CXX", "${get("PREFIX")}clang++")
          put("CC", "${get("PREFIX")}clang")
          //environmentKonan(this@registerXtrasGitLibrary, target)
        } else if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.MACOS_ARM64 || target == KonanTarget.LINUX_X64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
          environmentKonan(this@registerXtrasGitLibrary, target, this@sqlite)
        }

        if (target.family == Family.MINGW && HostManager.hostIsLinux) {
          put("PATH", pathOf("/usr/x86_64-w64-mingw32/bin", get("PATH")))
        }
      }
    }

    buildCommand { target->
      writer.println("""
          [ ! -f Makefile ] && ./configure --host=${target.hostTriplet} --prefix=${buildDir(target).mixedPath} --disable-tcl --disable-readline 
          make
          make install          
        """.trimIndent())
    }



/*
    compileSource {
      if (it == KonanTarget.MINGW_X64 && HostManager.hostIsLinux) {
        doFirst {
          listOf("lemon", "mkkeywordhash", "mksourceid", "src-verify").forEach { exe ->
            exec {
              this.workingDir = this@compileSource.workingDir
              commandLine("ln", "-s", exe, "${exe}.exe")
            }
          }
        }
      }
      xtrasCommandLine("make")
    }


    installSource {
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

    }*/

    block()
  }
