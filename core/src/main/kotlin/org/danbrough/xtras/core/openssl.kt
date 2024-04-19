package org.danbrough.xtras.core

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.konanDir
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.tasks.PackageTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.danbrough.xtras.xtrasCommandLine
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

fun Project.openssl(libName: String = "openssl", block: XtrasLibrary.() -> Unit = {}) =
  registerXtrasGitLibrary<XtrasLibrary>(libName) {
    cinterops {
      declaration = """
        #staticLibraries =  libcrypto.a libssl.a
        headerFilter = openssl/**
        #headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
        excludeDependentModules = true
        linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto -L/usr/lib 
        linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.mingw = -lm -lssl -lcrypto
        compilerOpts.android = -D__ANDROID_API__=${xtras.androidConfig.ndkApiVersion}  
        compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        #compilerOpts = -static
        
        """.trimIndent()

      targetWriterFilter = { target -> target == KonanTarget.LINUX_ARM64 }

      afterEvaluate {
        tasks.withType<CInteropProcess> {
          if (konanTarget in setOf(KonanTarget.LINUX_ARM64)) {
            dependsOn(PackageTaskName.EXTRACT.taskName(this@registerXtrasGitLibrary, konanTarget))
          }
        }
      }
    }

    environment { target ->
      put("MAKEFLAGS", "-j8")
      put("CFLAGS", "-Wno-unused-command-line-argument -Wno-macro-redefined")
      if (target.family == Family.ANDROID)
        put("ANDROID_NDK_ROOT", xtras.androidConfig.ndkDir)

      if (target == KonanTarget.LINUX_ARM64) {
        //put("PATH",pathOf(project.xtrasKon))

        val depsDir = project.konanDir.resolve("dependencies")
        val llvmPrefix = if (HostManager.hostIsLinux) "llvm-" else "apple-llvm"
        val llvmDir = depsDir.listFiles()?.first {
          it.isDirectory && it.name.startsWith(llvmPrefix)
        } ?: error("No directory beginning with \"llvm-\" found in ${depsDir.mixedPath}")
        put("PATH", pathOf(llvmDir.resolve("bin"), get("PATH")))
        val clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")}" +
              " --sysroot=${
                depsDir.resolveAll(
                  "aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2",
                  "aarch64-unknown-linux-gnu",
                  "sysroot"
                )
              }"
        put("CLANG_ARGS", clangArgs)
        put("CC", "clang $clangArgs")
        put("CXX", "clang++ $clangArgs")
      } else if (target == KonanTarget.MINGW_X64) {
        /*put("CC", "x86_64-w64-mingw32-gcc")
        put("AR", "x86_64-w64-mingw32-ar")
        put("RANLIB", "x86_64-w64-mingw32-ranlib")
        put("RC", "x86_64-w64-mingw32-windres")*/
        //put("PREFIX", "x86_64-w64-mingw32-")
      }
    }


    configureSource { target ->
      outputs.file(workingDir.resolve("Makefile"))

      val args = mutableListOf(
        "./Configure",
        target.opensslPlatform,
        "no-tests",
        "threads",
        "zlib",
//      "--with-zlib-include=${zlib.libsDir(target).resolve("include")}",
//      "--with-zlib-lib=${zlib.libsDir(target).resolve("lib").resolve("libz.a")}",
        "--prefix=${buildDir(target)}",
        "--libdir=lib",
      )

      if (target.family == Family.ANDROID)
        args += "-D__ANDROID_API__=${xtras.androidConfig.ndkApiVersion}"
      else if (target == KonanTarget.MINGW_X64) {
        args += "--cross-compile-prefix=x86_64-w64-mingw32-"
      }


      xtrasCommandLine(args)
    }

    compileSource {
      xtrasCommandLine("make")
    }

    installSource {
      xtrasCommandLine("make", "install_sw")
    }

    block()
  }


val KonanTarget.opensslPlatform: String
  get() = when (this) {
    KonanTarget.LINUX_X64 -> "linux-x86_64"
    KonanTarget.LINUX_ARM64 -> "linux-aarch64"
    //  KonanTarget.LINUX_ARM32_HFP -> "linux-armv4"
//    KonanTarget.LINUX_MIPS32 -> TODO()
//    KonanTarget.LINUX_MIPSEL32 -> TODO()
    KonanTarget.ANDROID_ARM32 -> "android-arm"
    KonanTarget.ANDROID_ARM64 -> "android-arm64"
    KonanTarget.ANDROID_X86 -> "android-x86"
    KonanTarget.ANDROID_X64 -> "android-x86_64"
    KonanTarget.MINGW_X64 -> "mingw64"
    //KonanTarget.MINGW_X86 -> "mingw"


    KonanTarget.MACOS_X64 -> "darwin64-x86_64"
    KonanTarget.MACOS_ARM64 -> "darwin64-arm64-cc"
    //KonanTarget.IOS_ARM32 -> "ios-cross" //ios-cross ios-xcrun ios64-cross ios64-xcrun iossimulator-xcrun iphoneos-cross

    KonanTarget.IOS_ARM64 -> "ios64-cross" //ios-cross ios-xcrun
    //KonanTarget.IOS_SIMULATOR_ARM64 -> "iossimulator-xcrun"
    KonanTarget.IOS_X64 -> "ios64-cross"

    else -> throw Error("$this not supported for openssl")
  }


fun Project.openSSLMessage() {
  logInfo("openSSLMessage() called from $group:$version")
}