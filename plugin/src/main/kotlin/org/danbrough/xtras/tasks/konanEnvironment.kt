package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasEnvironment
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xtrasKonanDir

fun XtrasEnvironment.konanEnvironment(env: ScriptEnvironment) {

  val konanPrebuiltDir = project.xtrasKonanDir.listFiles()
    ?.filter { it.isDirectory && it.name.startsWith("kotlin-native-prebuilt") }
    ?.maxOrNull()?.absolutePath
  val depsDir = project.xtrasKonanDir.resolve("dependencies")
  project.xDebug("konanPrebuiltDir: $konanPrebuiltDir depsDir: $depsDir")
}
/*
fun XtrasEnvironment.environmentKonan(
  library: XtrasLibrary,
  target: KonanTarget,
  project: Project
) {
  //put("PATH",pathOf(project.xtrasKon))
  val depsDir = library.project.konanDir.resolve("dependencies")
  val llvmPrefix = if (HostManager.hostIsLinux || HostManager.hostIsMingw) "llvm-" else "apple-llvm"
  val llvmDir = depsDir.listFiles()?.firstOrNull {
    it.isDirectory && it.name.startsWith(llvmPrefix)
  } ?: error("No directory beginning with \"llvm-\" found in ${depsDir.mixedPath}")
  put("PATH", project.pathOf(llvmDir.resolve("bin"), get("PATH")))
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

      KonanTarget.LINUX_X64 ->
        "--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")}" +
            " --sysroot=${
              depsDir.resolveAll(
                "x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2",
                "x86_64-unknown-linux-gnu",
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


      /*			KonanTarget.ANDROID_ARM64 ->
              "--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("target-toolchain-2-linux-android_ndk")}" +
                  " --sysroot=${
                    depsDir.resolveAll(
                      "target-toolchain-2-linux-android_ndk",
                      "aarch64-linux-android",
                    )
                  }"*/


      else -> error("Unsupported konan target: $target")
    }
  put("CLANG_ARGS", clangArgs)
  put("CC", "clang $clangArgs")
  put("CXX", "clang++ $clangArgs")

}


 */