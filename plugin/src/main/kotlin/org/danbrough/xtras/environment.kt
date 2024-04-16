@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

typealias XtrasEnvironment = MutableMap<String, Any>
typealias XtrasEnvironmentConfig = XtrasEnvironment.(target: KonanTarget?) -> Unit

val XtrasExtension.XTRAS_DEFAULT_ENVIRONMENT: XtrasEnvironmentConfig
  get() = { target ->
    put("MAKEFLAGS", "-j${Runtime.getRuntime().availableProcessors()}")
    if (target?.family == Family.ANDROID)
      ndkEnvironment(this, target)
  }


private fun XtrasExtension.ndkEnvironment(env: XtrasEnvironment, target: KonanTarget) {

  val archFolder = when {
    HostManager.hostIsLinux -> "linux-x86_64"
    HostManager.hostIsMac -> "darwin-x86_64"
    HostManager.hostIsMingw -> "windows-x86_64"
    else -> error("Unhandled host: ${HostManager.host}")
  }

  env.apply {
    put(
      "PATH",
      pathOf(
        androidConfig.ndkDir.resolve("bin"),
        androidConfig.ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin"),
        get("PATH")
      )
    )

    //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
    put("PREFIX", "${target.hostTriplet}${androidConfig.compileSDKVersion}-")
    put("CC", "clang")
    put("CXX", "clang++")
    put("AR", "llvm-ar")
    put("RANLIB", "ranlib")
  }
}



