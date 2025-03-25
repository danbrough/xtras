package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.KonanTarget

val KonanTarget.xtrasName: String
  get() = name.split("_").let {
    it[0] + it.subList(1, it.size)
      .joinToString("") { part -> part.replaceFirstChar { firstChar -> firstChar.uppercase() } }
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
