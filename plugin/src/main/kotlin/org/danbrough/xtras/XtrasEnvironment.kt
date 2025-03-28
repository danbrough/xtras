package org.danbrough.xtras

import org.danbrough.xtras.Xtras.Companion.xtras
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

class XtrasEnvironment(val project: Project) {
  companion object {
    private const val XTRAS_ENV = "$XTRAS_EXTN_NAME.env"
  }

  val pathDefault = project.xtrasProperty<String>("$XTRAS_ENV.bin") {
    if (HostManager.hostIsLinux) "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
    else if (HostManager.hostIsMac) "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
    else TODO("Need to set a XtrasEnvironment.pathDefault for ${HostManager.host}")
  }
}


/*
fun XtrasEnvironment.environmentNDK(xtras: Xtras, target: KonanTarget, project: Project) {
  put("ANDROID_NDK_ROOT", xtras.androidConfig.ndkDir)
  put("ANDROID_NDK", xtras.androidConfig.ndkDir)

  val archFolder = when {
    HostManager.hostIsLinux -> "linux-x86_64"
    HostManager.hostIsMac -> "darwin-x86_64"
    HostManager.hostIsMingw -> "windows-x86_64"
    else -> error("Unhandled host: ${HostManager.host}")
  }

  val ndkPath = project.pathOf(
    xtras.androidConfig.ndkDir.resolve("bin"),
    xtras.androidConfig.ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin"),
    get("PATH")
  )
  xtras.project.logTrace("environmentNDK: NDK_PATH: $ndkPath")
  put(
    "PATH",
    ndkPath
  )

  //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
  put("PREFIX", "${target.hostTriplet}${xtras.androidConfig.ndkApiVersion}-")

  put("CC", "clang")
  put("CXX", "clang++")

  put("AR", "llvm-ar")
  put("RANLIB", "ranlib")

}
 */

fun XtrasEnvironment.androidEnvironment(
  env: ScriptEnvironment = ScriptEnvironment(),
  target: KonanTarget,
): ScriptEnvironment {
  val xtras = project.xtras
  val ndkDir = xtras.android.ndkDir.get()

  env["ANDROID_NDK_ROOT"] = ndkDir
  env["ANDROID_NDK"] = ndkDir

  val archFolder = when {
    HostManager.hostIsLinux -> "linux-x86_64"
    HostManager.hostIsMac -> "darwin-x86_64"
    HostManager.hostIsMingw -> "windows-x86_64"
    else -> error("Unhandled host: ${HostManager.host}")
  }

  val ndkPath = pathOf(
    ndkDir.resolve("bin"), ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin"), env["PATH"]
  )
  project.xInfo("environmentNDK: NDK_PATH: $ndkPath")
  env["PATH"] = ndkPath


  env["PREFIX"] = "${target.hostTriplet}${xtras.android.ndkVersion.get()}-"
  env["CC"] = "clang"
  env["CXX"] = "clang++"
  env["AR"] = "llvm-ar"
  env["RANLIB"] = "ranlib"

  return env
}

fun XtrasEnvironment.konanEnvironment(
  env: ScriptEnvironment = ScriptEnvironment(),
  target: KonanTarget? = null,
): ScriptEnvironment {

  val konanPrebuiltDir = project.xtrasKonanDir.listFiles()
    ?.filter { it.isDirectory && it.name.startsWith("kotlin-native-prebuilt") }?.maxOrNull()

  val depsDir = project.xtrasKonanDir.resolve("dependencies")
  val llvmPrefix = if (HostManager.hostIsLinux || HostManager.hostIsMingw) "llvm-" else "apple-llvm"
  val llvmDir = depsDir.listFiles()?.firstOrNull {
    it.isDirectory && it.name.startsWith(llvmPrefix)
  } ?: error("No directory beginning with \"llvm-\" found in $depsDir")

  env["PATH"] = pathOf(konanPrebuiltDir?.resolve("bin"), llvmDir.resolve("bin"), env["PATH"])

  target ?: return env

  val clangArgs = when {
    HostManager.hostIsLinux -> {
      when (target) {
        KonanTarget.LINUX_X64 -> "--target=${target.hostTriplet} --gcc-toolchain=${
          depsDir.resolve(
            "x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2"
          )
        }" + " --sysroot=${
          depsDir.resolveAll(
            "x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2",
            "x86_64-unknown-linux-gnu",
            "sysroot"
          )
        }"

        KonanTarget.LINUX_ARM64 -> "--target=${target.hostTriplet} --gcc-toolchain=${
          depsDir.resolve(
            "aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2"
          )
        }" + " --sysroot=${
          depsDir.resolveAll(
            "aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2",
            "aarch64-unknown-linux-gnu",
            "sysroot"
          )
        }"

        KonanTarget.MINGW_X64 -> "--target=${target.hostTriplet} --gcc-toolchain=${
          depsDir.resolve("msys2-mingw-w64-x86_64-2")
        }" + " --sysroot=${
          depsDir.resolveAll(
            "msys2-mingw-w64-x86_64-2", "x86_64-w64-mingw32", "sysroot"
          )
        }"

        /*        KonanTarget.ANDROID_ARM64 -> "--target=${target.hostTriplet} --gcc-toolchain=${
                  depsDir.resolve(
                    "target-toolchain-2-linux-android_ndk"
                  )
                }" + " --sysroot=${
                  depsDir.resolveAll(
                    "target-toolchain-2-linux-android_ndk",
                    "aarch64-linux-android",
                  )
                }"*/

        else -> error("Unhandled target: $target")
      }
    }

    else -> TODO("Support: ${HostManager.host}")
  }

  env["CLANG_ARGS"] = clangArgs
  env["CC"] = "clang $clangArgs"
  env["CXX"] = "clang++ $clangArgs"


  return env
}
