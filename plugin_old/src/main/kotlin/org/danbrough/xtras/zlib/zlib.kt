package org.danbrough.xtras.zlib

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.registerGitLibrary
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


fun Project.zlib(
  block: LibraryExtension.() -> Unit = {}
) = registerGitLibrary<LibraryExtension>("zlib") {


  cinterops {
    headers = """
      headers = zlib.h zconf.h
      #linkerOpts = -lz
      staticLibraries = libz.a
      """.trimIndent()
  }


  fun makeCommand(target: KonanTarget): MutableList<String> {
    val cmd = mutableListOf("make")
    if (target.family == Family.MINGW) {
      val buildDir = buildDir(target)
      cmd.addAll("-f win32/Makefile.gcc PREFIX=x86_64-w64-mingw32-".split(' '))
      cmd.add("LIBRARY_PATH=${buildDir.resolve("lib").absolutePath}")
      cmd.add("INCLUDE_PATH=${buildDir.resolve("include").absolutePath}")
      cmd.add("BINARY_PATH=${buildDir.resolve("bin").absolutePath}")
    }
    return cmd
  }

  configureSource { target ->

    outputs.file(workingDir.resolve("zlib.pc"))

    doFirst {
      project.logWarn("RUNNING CONFIGURE WITH ${commandLine.joinToString(" ")}")
    }

    onlyIf { target != KonanTarget.MINGW_X64 }

    val args = mutableListOf<String>()


    args.add("./configure")
    args.add("--prefix=${buildDir(target).absolutePath}")

    commandLine(args)
  }

  compileSource { target ->
    val buildDir = buildDir(target)
    outputs.dir(buildDir)
    commandLine(makeCommand(target))
  }

  installSource { target ->
    commandLine(makeCommand(target).also { it.add("install") })
    doLast {
      copy {
        from(sourceDir(target)) {
          include("example")
          include("example.exe")
          include("minigzip")
          include("minigzip.exe")
        }
        into(buildDir(target).resolve("bin"))
      }
    }

  }


  block()
}
