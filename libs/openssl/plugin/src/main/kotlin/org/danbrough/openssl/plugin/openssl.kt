package org.danbrough.openssl.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.XtrasPlugin
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.environmentApple
import org.danbrough.xtras.git.git
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.tasks.buildScript
import org.danbrough.xtras.tasks.cinterops
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasKonanDir
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


class OpenSSLPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.registerOpensslLibrary()
  }
}

private fun Project.registerOpensslLibrary() {
  println("registerOpensslLibrary: rootProject: ${rootProject.name} - xtras: ${rootProject.extensions.findByType<XtrasPlugin>()}")
  val xtrasEnv = xtras.environment
  val androidSdkVersion = 21 //xtras.android.sdkVersion.get()

  xtrasRegisterLibrary<XtrasLibrary>("openssl") {

    cinterops {
      declaration {
        println(
          """
        ##staticLibraries =  libcrypto.a libssl.a
        ##headerFilter = openssl/**
        #headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
        #excludeDependentModules = true
        #linkerOpts.linux = -lssl -lcrypto
        #linkerOpts.android = -lssl -lcrypto
        #linkerOpts.macos = -lssl -lcrypto
        #linkerOpts.ios =  -lssl -lcrypto
        ##linkerOpts.ios = -ldl -lc -lm -lssl -lcrypto
        #linkerOpts.mingw = -lm -lssl -lcrypto
        #compilerOpts.android = -D__ANDROID_API__=$androidSdkVersion -I${xtrasKonanDir.resolve("dependencies/target-toolchain-2-linux-android_ndk/sysroot/usr/include").absolutePath}
        #compilerOpts = -fPIC -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        ##compilerOpts = -static

        """.trimIndent()
        )
      }

      extraCode {
        println(
          """
            #include<stdio.h>
            void testFunction(){
              printf("Test Openssl Function Works!!!\n");
            }
          """.trimIndent()
        )
      }
    }

    git {
      xTrace("configuring git for $name url:$url commit:?")
    }

    buildScript {
      //outputs.file(workingDir.resolve("Makefile"))
      val konanTarget = target.get()
      outputDirectory.convention(provider { installDirMap(konanTarget) })


      clearEnvironment()
      defaultEnvironment()
      val env = ScriptEnvironment(environment)
      when (konanTarget.family) {
        Family.ANDROID -> {
          environment(xtrasEnv.androidEnvironment(env, target = konanTarget))
          env["CFLAGS"] = buildString {
            //        var cflags = "-Wno-unused-command-line-argument -Wno-macro-redefined -Os"
            append("-Wno-macro-redefined ")
            append("-fPIC ")
            env["CFLAGS"]?.also {
              append(it)
            }
          }
        }

        Family.OSX, Family.IOS -> environment(
          xtrasEnv.environmentApple(
            env, target = konanTarget
          )
        )

        else -> environment(xtrasEnv.konanEnvironment(env, target = konanTarget))
      }


      script {
        xInfo("openssl: writing taskConfigureSource script..")
        println("echo running configure at `date` ..")
        println("if [ ! -f Makefile ]; then")
        println("./Configure ${konanTarget.opensslPlatform} \\")
        if (konanTarget.family == Family.ANDROID) println("-D__ANDROID_API__=$androidSdkVersion \\")
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

    KonanTarget.IOS_ARM64 -> "ios64-xcrun" //ios64-cross ios-xcrun
    KonanTarget.IOS_SIMULATOR_ARM64 -> "iossimulator-xcrun"
    KonanTarget.IOS_X64 -> "ios64-cross"

    else -> throw Error("$this not supported for openssl")
  }


