package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.Locale


val KonanTarget.kotlinTargetName: String
  get() {
    if (family == Family.ANDROID) {
      return when (this) {
        KonanTarget.ANDROID_X64 -> "androidNativeX64"
        KonanTarget.ANDROID_X86 -> "androidNativeX86"
        KonanTarget.ANDROID_ARM64 -> "androidNativeArm64"
        KonanTarget.ANDROID_ARM32 -> "androidNativeArm32"
        else -> throw Error("Unhandled android target $this")
      }
    }
    return name.split("_").joinToString("") {
      it.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
          Locale.getDefault()
        ) else it.toString()
      }
    }.decapitalized()
  }


val KonanTarget.Companion.targetNameMap: Map<String, KonanTarget>
  get() = predefinedTargets.mapKeys { keyEntry ->
    keyEntry.key.split('_').joinToString("") {
      it.let<CharSequence, CharSequence> {
        if (it.isEmpty()) it else it[0].titlecase(Locale.getDefault()) + it.substring(
          1
        )
      }
    }.decapitalized()
  }


val KonanTarget.hostTriplet: String
  get() = when (this) {
    KonanTarget.LINUX_ARM64 -> "aarch64-unknown-linux-gnu"
    KonanTarget.LINUX_X64 -> "x86_64-unknown-linux-gnu"
    //KonanTarget.LINUX_ARM32_HFP -> "arm-linux-gnueabihf"
    KonanTarget.ANDROID_ARM32 -> "armv7a-linux-androideabi"
    KonanTarget.ANDROID_ARM64 -> "aarch64-linux-android"
    KonanTarget.ANDROID_X64 -> "x86_64-linux-android"
    KonanTarget.ANDROID_X86 -> "i686-linux-android"
    KonanTarget.MACOS_X64 -> "x86_64-apple-darwin"
    KonanTarget.MACOS_ARM64 -> "aarch64-apple-darwin"
    KonanTarget.MINGW_X64 -> "x86_64-w64-mingw32"
    //KonanTarget.MINGW_X86 -> "x86-w64-mingw32"
    //KonanTarget.IOS_ARM32 -> "arm32-apple-darwin"
    KonanTarget.IOS_ARM64 -> "aarch64-apple-ios" //"aarch64-ios-darwin"
    //KonanTarget.IOS_SIMULATOR_ARM64 -> "aarch64-iossimulator-darwin"
    KonanTarget.IOS_X64 -> "x86_64-apple-ios-simulator" //"x86_64-ios-darwin"


    KonanTarget.TVOS_ARM64 -> "aarch64-apple-tvos"
    //KonanTarget.TVOS_SIMULATOR_ARM64 -> "aarch64-tvossimulator-darwin"
    KonanTarget.TVOS_X64 -> "x86_64-apple-tvos-simulator"

    //KonanTarget.WATCHOS_ARM32 -> "arm32-watchos-darwin"
    KonanTarget.WATCHOS_ARM64 -> "arm64_32-apple-watchos"
    //KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "aarch64-watchossimulator-darwin"
    KonanTarget.WATCHOS_X64 -> "x86_64-apple-watchos-simulator"
    //KonanTarget.WATCHOS_X86 -> "x86-watchos-darwin"
    else -> TODO("Add KonanTarget.hostTriplet for $this")

  }


val KonanTarget.androidLibDir: String?
  get() = when (this) {
    KonanTarget.ANDROID_ARM32 -> "armeabi-v7a"
    KonanTarget.ANDROID_ARM64 -> "arm64-v8a"
    KonanTarget.ANDROID_X64 -> "x86_64"
    KonanTarget.ANDROID_X86 -> "x86"
    else -> null
  }

val KonanTarget.sharedLibExtn: String
  get() = when {
    family.isAppleFamily -> "dylib"
    family == Family.MINGW -> "dll"
    else -> "so"
  }

val KonanTarget.envLibraryPathName: String
  get() = when {
    family.isAppleFamily -> "DYLD_LIBRARY_PATH"
    else -> "LD_LIBRARY_PATH"
  }


val KonanTarget.goOS: String?
  get() = when (family) {
    Family.OSX -> "darwin"
    Family.IOS, Family.TVOS, Family.WATCHOS -> "ios"
    Family.LINUX -> "linux"
    Family.MINGW -> "windows"
    Family.ANDROID -> "android"
    //Family.WASM -> null
    else -> null
  }

val KonanTarget.goArch: String
  get() = when (architecture) {
    Architecture.ARM64 -> "arm64"
    Architecture.X64 -> "amd64"
    //Architecture.WASM32 -> "wasm"
    else -> error("unsupported KonanTarget.goArch $this")
  }

/**
 * Set of [KonanTarget] that support JNI implementations
 */
val jniTargets = setOf(
  KonanTarget.MINGW_X64,
  KonanTarget.MACOS_X64,
  KonanTarget.MACOS_ARM64,
  KonanTarget.LINUX_X64,
  KonanTarget.LINUX_ARM64,
  KonanTarget.ANDROID_ARM64,
  KonanTarget.ANDROID_ARM32,
  KonanTarget.ANDROID_X64
)

/**
 * Whether the [KonanTarget] is in [jniTargets]
 */
val KonanTarget.supportsJNI: Boolean
  get() = this in jniTargets


val KonanTarget.macArch: String?
  get() = when (this) {
    KonanTarget.MACOS_X64 -> "x86_64"
    KonanTarget.MACOS_ARM64 -> "arm64"
    else -> null
  }



