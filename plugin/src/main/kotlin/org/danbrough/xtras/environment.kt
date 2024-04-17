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
      val home = System.getProperty("user.home")
      clear()
      put("HOME", home)
    }
    if (HostManager.hostIsMingw) {
      put(
        "PATH", pathOf(
          project.xtrasMsysDir.resolveAll("mingw64", "bin"),
          project.xtrasMsysDir.resolveAll("usr", "local", "bin"),
          project.xtrasMsysDir.resolveAll("usr", "bin"),
          project.xtrasMsysDir.resolveAll("bin"),
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

fun XtrasEnvironment.environmentKonan(library: XtrasLibrary, target: KonanTarget) {
  //put("PATH",pathOf(project.xtrasKon))
  val depsDir = library.project.konanDir.resolve("dependencies")
  val llvmPrefix = if (HostManager.hostIsLinux || HostManager.hostIsMingw) "llvm-" else "apple-llvm"
  val llvmDir = depsDir.listFiles()?.firstOrNull() {
    it.isDirectory && it.name.startsWith(llvmPrefix)
  } ?: error("No directory beginning with \"llvm-\" found in ${depsDir.mixedPath}")
  put("PATH", pathOf(llvmDir.resolve("bin"), get("PATH")))
  val clangArgs =
    when (target) {
      KonanTarget.LINUX_ARM64 ->
        "--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")}" +
            " --sysroot=${
              depsDir.resolveAll(
                "aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2",
                "aarch64-unknown-linux-gnu",
                "sysroot"
              )
            }"

      KonanTarget.MINGW_X64 ->
        "--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("msys2-mingw-w64-x86_64-2")}" +
            " --sysroot=${
              depsDir.resolveAll(
                "msys2-mingw-w64-x86_64-2",
                "x86_64-w64-mingw32",
                "sysroot"
              )
            }"

      else -> error("Unsupported konan target: $target")
    }
  put("CLANG_ARGS", clangArgs)
  put("CC", "clang")
  put("CXX", "clang++")

}

