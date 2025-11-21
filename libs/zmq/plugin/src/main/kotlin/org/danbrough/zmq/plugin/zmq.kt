package org.danbrough.zmq.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.git.git
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.tasks.buildScript
import org.danbrough.xtras.tasks.cinterops
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


class ZmqPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.registerZmqLibrary()
  }
}

private fun Project.registerZmqLibrary() {
  //extensions.findByName("sodium") as? XtrasLibrary ?: error("sodium is not configured")

  xtrasRegisterLibrary<XtrasLibrary>("zmq") {
    cinterops {
      declaration {
        println(
          """
        ##staticLibraries =  libcrypto.a libssl.a
        #headerFilter = sodium/**
        headers = zmq.h zmq_utils.h
        #excludeDependentModules = true
        linkerOpts = -lsodium -lzmq
        #linkerOpts.linux = -ldl -lc -lm -lsqlite3 
        #linkerOpts.android = -ldl -lc -lm -lsqlite3
        #linkerOpts.macos = -ldl -lc -lm -lsqlite3
        #linkerOpts.ios = -ldl -lc -lm -lsqlite3
        #linkerOpts.mingw = -ldl -lc -lm -lsqlite3
        #compilerOpts.android = -D__ANDROID_API__=${xtras.android.sdkVersion.get()}  
        #compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        
       
        """.trimIndent()
        )
      }

      extraCode {
        println(
          """
            #include<stdio.h>
            void testFunction(){
              printf("Zmq Test Function Works!!!\n");
            }
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

      doFirst {
        clearEnvironment()
        defaultEnvironment()
        val env = ScriptEnvironment(environment)
        /*
        "$CFLAGS -march=armv8-a+crypto+aes"
         */
        if (konanTarget.family == Family.ANDROID) {
          environment(xtras.environment.androidEnvironment(env, target = konanTarget))
          env["CFLAGS"] = buildString {
            //        var cflags = "-Wno-unused-command-line-argument -Wno-macro-redefined -Os"
            append("-Wno-macro-redefined ")
            env["CFLAGS"]?.also {
              append(it)
            }
          }
        } else {
          environment(xtras.environment.konanEnvironment(env, target = konanTarget))
        }

        env["sodium_CFLAGS"] =
          "-I/files/cache/xtras/lib/sodium_${target.get().kotlinTargetName}_1.0.20/include -Wno-error=unused-command-line-argument -Wno-gnu-statement-expression"
        env["sodium_LIBS"] =
          "-L/files/cache/xtras/lib/sodium_${target.get().kotlinTargetName}_1.0.20/lib -lc -lm -lsodium "
        env["CFLAGS"] = "${env["CFLAGS"] ?: ""} ${env["sodium_CFLAGS"]}"
        env["CPPFLAGS"] = env["CFLAGS"].toString()
        env["LDFLAGS"] =
          "${env["LDFLAGS"] ?: ""} ${env["sodium_LIBS"]} -Wno-error=unused-command-line-argument  "
        env["CLANG_ARGS"] = "${env["CLANG_ARGS"] ?: ""} -Wno-error=unused-command-line-argument "
      }

      script {
        xInfo("zmq: writing build script..for ${target.get().name}")
        println("if [ ! -f configure ]; then")
        println("   ./autogen.sh")
        println("fi")

        println("echo running configure at `date` ..")
        println("if [ ! -f Makefile ]; then")
        println("./configure --prefix=\"${outputDirectory.get()}\" \\")
        println("   --with-libsodium --enable-shared=yes --with-gnu-ld \\")
        println("   --enable-static=no --enable-shared=yes  --enable-ws \\")
        println("   --enable-libbsd=no --enable-libunwind=no \\")
        println("   --host=${konanTarget.hostTriplet}")
        /*println("./Configure ${konanTarget.opensslPlatform} \\")
        if (konanTarget.family == Family.ANDROID) println("-D__ANDROID_API__=${xtras.android.sdkVersion.get()} \\")
        println("no-engine no-asm no-tests threads zlib --prefix=\"${outputDirectory.get()}\" --libdir=lib")*/
        println("fi || exit 1")

        println("echo source configured .. building in 2")
        //println("sleep 2")
        println("make -j4 || exit 1")
        println("make install")
      }
      /*
      export sodium_CFLAGS="-I/files/cache/xtras/lib/sodium_linuxX64_1.0.20/include/"
export sodium_LIBS="-L/files/cache/xtras/lib/sodium_linuxX64_1.0.20/lib -lsodium"
export CFLAGS="$CFLAGS $sodium_CFLAGS"
export LDFLAGS="$LDFALGS $sodium_LIBS"

echo running configure at `date` ..
if [ ! -f Makefile ]; then
./configure --prefix="/files/cache/xtras/build/zmq_linuxX64_4.3.5" \
   --with-libsodium --enable-shared=yes \
        --enable-libbsd=no --enable-libunwind=no \
   --enable-static=no --enable-shared=yes --includedir=/usr/include \
   --host=x86_64-unknown-linux-gnu
fi || exit 1
#echo source configured .. building in 2
#sleep 2
make -j4 || exit 1
make install




      cd "$(dirname "$0")"
. /files/cache/xtras/src/zmq_linuxX64_4.3.5/xtras_xtrasZmqBuildLinuxX64_linuxX64_env.sh

export CFLAGS="-I/files/cache/xtras/lib/sodium_linuxX64_1.0.20/include"
export LDFLAGS="-L/files/cache/xtras/lib/sodium_linuxX64_1.0.20/lib -lsodium"
export sodium_CFLAGS="$CFLAGS"
export sodium_LIBS="$LDFLAGS"
#export CFLAGS="$sodium_CFLAGS -pthread"
#export CPPFLAGS="$sodium_CFLAGS"
#export LDFLAGS="$sodium_LIBS -lm -lc -lsodium"

if [ ! -f configure ]; then
   ./autogen.sh
fi


#export LD_LIBRARY_PATH=/files/cache/xtras/lib/sodium_linuxX64_1.0.20/lib
echo running configure at `date` ..
#if [ ! -f Makefile ]; then
./configure --prefix="/files/cache/xtras/build/zmq_linuxX64_4.3.5" \
   --with-libsodium \
   --enable-static=no --enable-shared=yes --includedir=/usr/include \
   --host=x86_64-unknown-linux-gnu  \
	--disable-libbsd --disable-libunwind
#fi || exit 1
#echo source configured .. building in 2
#sleep 2
make || exit 1
make install

       */
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


