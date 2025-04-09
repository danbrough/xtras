package org.danbrough.openssl.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.git.git
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.tasks.buildScript
import org.danbrough.xtras.tasks.cinterops
import org.danbrough.xtras.tasks.konanDepsTaskName
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


class OpenSSLPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.registerOpensslLibrary()
  }
}

private fun Project.registerOpensslLibrary() {
  xtrasRegisterLibrary<XtrasLibrary>("openssl") {
    cinterops {
      declaration {
        println(
          """
        #staticLibraries =  libcrypto.a libssl.a
        headerFilter = openssl/**
        #headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
        excludeDependentModules = true
        linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto -L/usr/lib 
        linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.ios = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.mingw = -lm -lssl -lcrypto
        compilerOpts.android = -D__ANDROID_API__=${xtras.android.sdkVersion.get()}  
        compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        #compilerOpts = -static
       
        """.trimIndent()
        )
      }
    }

    git {
      xTrace("configuring git for $name url:${url.get()} commit:${commit.get()}")
    }

    buildScript {
      //outputs.file(workingDir.resolve("Makefile"))
      val konanTarget = target.get()
      outputDirectory.convention(provider { installDirMap(konanTarget) })
      dependsOn(konanTarget.konanDepsTaskName)

      doFirst {
        clearEnvironment()
        defaultEnvironment()
        val env = ScriptEnvironment(environment)
        if (konanTarget.family == Family.ANDROID) {
          environment(xtras.environment.androidEnvironment(env, target = konanTarget))
          env["CFLAGS"] = buildString {
            //        var cflags = "-Wno-unused-command-line-argument -Wno-macro-redefined -Os"
            append("-Wno-macro-redefined ")
            env["CFLAGS"]?.also {
              append(it)
            }
          }
        } else environment(xtras.environment.konanEnvironment(env, target = konanTarget))
      }

      script {
        xInfo("openssl: writing taskConfigureSource script..")
        println("echo running configure at `date` ..")
        println("if [ ! -f Makefile ]; then")
        println("./Configure ${konanTarget.opensslPlatform} \\")
        if (konanTarget.family == Family.ANDROID) println("-D__ANDROID_API__=${xtras.android.sdkVersion.get()} \\")
        println("no-engine no-asm no-tests threads zlib --prefix=\"${outputDirectory.get()}\" --libdir=lib")
        println("fi || exit 1")

        println("echo source configured .. building in 2")
        println("sleep 2")
        println("make || exit 1")
        println("make install_sw")
      }
    }
  }
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


