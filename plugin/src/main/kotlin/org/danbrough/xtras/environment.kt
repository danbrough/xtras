@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

typealias XtrasEnvironment = MutableMap<String, Any>
typealias XtrasEnvironmentConfig = XtrasEnvironment.(target: KonanTarget) -> Unit

val XtrasExtension.INITIAL_ENVIRONMENT: XtrasEnvironmentConfig
  get() = { target ->
    if (cleanEnvironment) {
      project.logWarn("ENVIRONMENT: CLEAR")
      clear()
    }
    if (HostManager.hostIsMingw) {
      put(
        "PATH",
        pathOf(
          project.xtrasMsysDir.resolve("bin"),
          project.xtrasMsysDir.resolveAll("usr", "bin"),
          get("PATH")
        )
      )
    } else {
      put("PATH", pathOf("/bin", "/usr/bin", "/usr/local/bin", get("PATH")))
    }
    put("MAKEFLAGS", "-j${Runtime.getRuntime().availableProcessors()}")

    project.logTrace("INITIAL_ENVIRONMENT: target: $target")
    if (target.family == Family.ANDROID)
      environmentNDK(this, target)
  }


fun XtrasExtension.environmentNDK(env: XtrasEnvironment, target: KonanTarget) {

  val archFolder = when {
    HostManager.hostIsLinux -> "linux-x86_64"
    HostManager.hostIsMac -> "darwin-x86_64"
    HostManager.hostIsMingw -> "windows-x86_64"
    else -> error("Unhandled host: ${HostManager.host}")
  }

  env.apply {
    val ndkPath = pathOf(
      androidConfig.ndkDir.resolve("bin"),
      androidConfig.ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin"),
      get("PATH")
    )
    project.logTrace("environmentNDK: NDK_PATH: $ndkPath")
    put(
      "PATH",
      ndkPath
    )

    //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
    put("PREFIX", "${target.hostTriplet}${androidConfig.ndkApiVersion}-")
    put("CC", "clang")
    put("CXX", "clang++")
    put("AR", "llvm-ar")
    put("RANLIB", "ranlib")
  }
}



