package org.danbrough.openssl.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.git.git
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.tasks.buildScript
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
    project.run {

      xtrasRegisterLibrary<XtrasLibrary>("openssl") {
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
  }
}

//
//import org.danbrough.xtras.ENV_BUILD_DIR
//import org.danbrough.xtras.XtrasLibrary
//import org.danbrough.xtras.environmentKonan
//import org.danbrough.xtras.environmentNDK
//import org.danbrough.xtras.logInfo
//import org.danbrough.xtras.macArch
//import org.danbrough.xtras.registerXtrasGitLibrary
//import org.gradle.api.Project
//import org.jetbrains.kotlin.konan.target.Family
//import org.jetbrains.kotlin.konan.target.KonanTarget
//
//fun Project.openssl(libName: String = "openssl", block: XtrasLibrary.() -> Unit = {}) =
//  registerXtrasGitLibrary<XtrasLibrary>(libName) {
//    cinterops {
//      declaration = """
//        #staticLibraries =  libcrypto.a libssl.a
//        headerFilter = openssl/**
//        #headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
//        excludeDependentModules = true
//        linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto -L/usr/lib
//        linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
//        linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
//        linkerOpts.ios = -ldl -lc -lm -lssl -lcrypto
//        linkerOpts.mingw = -lm -lssl -lcrypto
//        compilerOpts.android = -D__ANDROID_API__=${xtras.androidConfig.ndkApiVersion}
//        compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
//        #compilerOpts = -static
//
//        """.trimIndent()
//
//      //targetWriterFilter = { target -> target == KonanTarget.LINUX_ARM64 }
//
//      buildCommand { target ->
//        writer.println("[ ! -f Makefile ] && ./Configure ${target.opensslPlatform} \\")
//
//        when {
//          target.family == Family.ANDROID ->
//            writer.println("-D__ANDROID_API__=${xtras.androidConfig.compileSDKVersion} \\")
//        }
//        writer.println("no-engine no-asm no-tests threads zlib --prefix=\$${ENV_BUILD_DIR} --libdir=lib")
//        writer.println("make || exit 1")
//        writer.println("make install_sw || exit 1")
//
//      }
//
//      /*      afterEvaluate {
//              tasks.withType<CInteropProcess> {
//                if (konanTarget in setOf(KonanTarget.LINUX_ARM64)) {
//                  dependsOn(PackageTaskName.EXTRACT.taskName(this@registerXtrasGitLibrary, konanTarget))
//                }
//              }
//            }*/
//    }
//
//    environment { target ->
//      if (target != null) {
//        var cflags = "-Wno-unused-command-line-argument -Wno-macro-redefined -Os"
//        if (target.family == Family.ANDROID)
//          environmentNDK(xtras, target, project)
//        else if (target.family == Family.LINUX)
//          environmentKonan(this@registerXtrasGitLibrary, target, project)
//        else if (target.family == Family.MINGW) {
//          put("CC", "x86_64-w64-mingw32-gcc")
//          put("RC", "x86_64-w64-mingw32-windres")
//        } else if (target.family.isAppleFamily) {
//          cflags += " -arch ${target.macArch}"
//        }
//        put("CFLAGS", cflags)
//      }
//    }
//
//
//    /*    configureSource { target ->
//          outputs.file(workingDir.resolve("Makefile"))
//          var command ="./Configure ${target.opensslPlatform} no-tests threads zlib --prefix=${buildDir(target)} --libdir=lib"
//
//          if (target.family == Family.ANDROID)
//            command += "-D__ANDROID_API__=${xtras.androidConfig.ndkApiVersion}"
//          else if (target == KonanTarget.MINGW_X64) {
//            command += "--cross-compile-prefix=x86_64-w64-mingw32-"
//          }
//
//
//          commandLine(xtras.sh,"-c",command)
//        }
//*/
//
//    block()
//  }
//
//

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


