package org.danbrough.zmq.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.XtrasEnvironment
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.tasks.ScriptTask
import org.danbrough.xtras.xDebug
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


fun XtrasEnvironment.androidEnvironmentZmq(
  script: ScriptTask,
  env: ScriptEnvironment,
  minSdkVersion: Int = 21,
): ScriptEnvironment {
  val xtras = project.xtras
  val ndkDir = xtras.android.ndkDir.get()
  val target = script.target.get()



  env["ANDROID_NDK_ROOT"] = ndkDir
  env["ANDROID_NDK"] = ndkDir
  env["ANDROID_NDK_HOME"] = ndkDir

  val archFolder = when {
    HostManager.hostIsLinux -> "linux-x86_64"
    HostManager.hostIsMac -> "darwin-x86_64"
    HostManager.hostIsMingw -> "windows-x86_64"
    else -> error("Unhandled host: ${HostManager.host}")
  }

  pathOf(
    ndkDir.resolve("bin"),
    ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin"),
    env["PATH"]
  )

  val androidBuildToolchain = ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder")
  project.xDebug("androidBuildToolchain: $androidBuildToolchain")
  if (!androidBuildToolchain.exists()) error("$androidBuildToolchain does not exist")

  env["ANDROID_BUILD_TOOLCHAIN"] = androidBuildToolchain.absolutePath
  val toolChainPath = androidBuildToolchain.resolve("bin")

  env["TOOLCHAIN_PATH"] = toolChainPath.checkedPath


  val (toolChainHost, toolChainComp, toolChainAbi, toolChainArch) =
    when (target) {
      KonanTarget.ANDROID_X64 -> listOf(
        "x86_64-linux-android", "x86_64-linux-android$minSdkVersion", "x86_64", "x86_64"
      )

      KonanTarget.ANDROID_X86 -> listOf(
        "i686-linux-android", "i686-linux-android$minSdkVersion", "x86", "x86"
      )

      KonanTarget.ANDROID_ARM64 -> listOf(
        "aarch64-linux-android", "aarch64-linux-android$minSdkVersion", "arm64-v8a", "aarch64"
      )

      KonanTarget.ANDROID_ARM32 -> listOf(
        "armv-linux-android", "armv7a-linux-android$minSdkVersion", "armeabi-v7a", "arm"
      )

      else -> error("Invalid target: $target")
    }

  env["TOOLCHAIN_HOST"] = toolChainHost
  env["TOOLCHAIN_COMP"] = toolChainComp
  env["TOOLCHAIN_ABI"] = toolChainAbi
  env["TOOLCHAIN_ARCH"] = toolChainArch

  env["ANDROID_BUILD_SYSROOT"] = androidBuildToolchain.resolve("sysroot").let {
    if (!it.exists()) error("$it does not exist")
    it.absolutePath
  }
  env["ANDROID_BUILD_PREFIX"] = script.outputDirectory.get()

  toolChainPath.resolve("$toolChainComp-clang")
  /*
      export ANDROID_BUILD_CC="${TOOLCHAIN_PATH}/${TOOLCHAIN_COMP}-clang"
    export ANDROID_BUILD_CXX="${TOOLCHAIN_PATH}/${TOOLCHAIN_COMP}-clang++"
    # Since NDK r22 the "platforms" dir got removed and the default linker is LLD
    if [ -d "${ANDROID_NDK_ROOT}/platforms" ]; then
       export ANDROID_BUILD_LD="${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-ld"
    else
       export ANDROID_BUILD_LD="${TOOLCHAIN_PATH}/ld"
    fi
    # Since NDK r24 this binary was removed due to LLVM being now the default
    if [ ! -x "${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-as" ]; then
        export ANDROID_BUILD_AS="${TOOLCHAIN_PATH}/llvm-as"
    else
        export ANDROID_BUILD_AS="${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-as"
    fi
    # Since NDK r23 those binaries were removed due to LLVM being now the default
    if [ ! -x "${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-ar" ]; then
        export ANDROID_BUILD_AR="${TOOLCHAIN_PATH}/llvm-ar"
        export ANDROID_BUILD_RANLIB="${TOOLCHAIN_PATH}/llvm-ranlib"
        export ANDROID_BUILD_STRIP="${TOOLCHAIN_PATH}/llvm-strip"
    else
        export ANDROID_BUILD_AR="${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-ar"
        export ANDROID_BUILD_RANLIB="${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-ranlib"
        export ANDROID_BUILD_STRIP="${TOOLCHAIN_PATH}/${TOOLCHAIN_HOST}-strip"
    fi

    if [ ! -x "${ANDROID_BUILD_CC}" ]; then
        ANDROID_BUILD_FAIL+=("The CC binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_CC}")
    fi

    if [ ! -x "${ANDROID_BUILD_CXX}" ]; then
        ANDROID_BUILD_FAIL+=("The CXX binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_CXX}")
    fi

    if [ ! -x "${ANDROID_BUILD_LD}" ]; then
        ANDROID_BUILD_FAIL+=("The LD binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_LD}")
    fi

    if [ ! -x "${ANDROID_BUILD_AS}" ]; then
        ANDROID_BUILD_FAIL+=("The AS binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_AS}")
    fi

    if [ ! -x "${ANDROID_BUILD_AR}" ]; then
        ANDROID_BUILD_FAIL+=("The AR binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_AR}")
    fi

    if [ ! -x "${ANDROID_BUILD_RANLIB}" ]; then
        ANDROID_BUILD_FAIL+=("The RANLIB binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_RANLIB}")
    fi

    if [ ! -x "${ANDROID_BUILD_STRIP}" ]; then
        ANDROID_BUILD_FAIL+=("The STRIP binary does not exist or is not executable")
        ANDROID_BUILD_FAIL+=("  ${ANDROID_BUILD_STRIP}")
    fi

    ANDROID_BUILD_OPTS+=("TOOLCHAIN=${ANDROID_BUILD_TOOLCHAIN}")
    ANDROID_BUILD_OPTS+=("CC=${ANDROID_BUILD_CC}")
    ANDROID_BUILD_OPTS+=("CXX=${ANDROID_BUILD_CXX}")
    ANDROID_BUILD_OPTS+=("LD=${ANDROID_BUILD_LD}")
    ANDROID_BUILD_OPTS+=("AS=${ANDROID_BUILD_AS}")
    ANDROID_BUILD_OPTS+=("AR=${ANDROID_BUILD_AR}")
    ANDROID_BUILD_OPTS+=("RANLIB=${ANDROID_BUILD_RANLIB}")
    ANDROID_BUILD_OPTS+=("STRIP=${ANDROID_BUILD_STRIP}")

   */



  return env
}