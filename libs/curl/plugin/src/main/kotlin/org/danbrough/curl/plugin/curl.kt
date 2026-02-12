package org.danbrough.curl.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.environmentApple
import org.danbrough.xtras.git.git
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.tasks.buildScript
import org.danbrough.xtras.tasks.cinterops
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


class CurlPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.registerCurlLibrary()
  }
}

private fun Project.registerCurlLibrary() {
  val sdkVersion = xtras.android.sdkVersion.get()
  val xtrasEnv = xtras.environment

  xtrasRegisterLibrary<XtrasLibrary>("curl") {
    cinterops {
      declaration {
        println(
          """
  
        #headerFilter = curl/**
        headers = curl/curl.h
        excludeDependentModules = true
        linkerOpts.linux = -ldl -lc -lm -lcurl 
        linkerOpts.android = -ldl -lc -lm -lcurl
        linkerOpts.macos = -ldl -lc -lm -lcurl
        linkerOpts.ios = -ldl -lc -lm -lcurl
        linkerOpts.mingw = -ldl -lc -lm -lcurl
        compilerOpts.android = -D__ANDROID_API__=$sdkVersion  
        compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        #compilerOpts = -static
       
        """.trimIndent()
        )
      }

      extraCode {
        println(
          """
            #include<stdio.h>
            void testFunction(){
              printf("Curl Test Function Works!!! Yay!!!\n");
            }
          """.trimIndent()
        )
      }
    }

    git {
      xTrace("configuring curl for $name url:$url commit:$commit")
    }

    buildScript {
      val konanTarget = target.get()

      //doFirst {
      clearEnvironment()
      defaultEnvironment()
      val env = ScriptEnvironment(environment)
      when (konanTarget.family) {
        Family.ANDROID -> {
          environment(xtrasEnv.androidEnvironment(env, target = konanTarget))
          env["CFLAGS"] = buildString {
            //        var cflags = "-Wno-unused-command-line-argument -Wno-macro-redefined -Os"
            append("-Wno-macro-redefined ")
            env["CFLAGS"]?.also {
              append(it)
            }
          }
        }

        Family.OSX -> environment(
          xtrasEnv.environmentApple(
            env,
            target = konanTarget
          )
        )

        else -> environment(xtrasEnv.konanEnvironment(project, env, target = konanTarget))
      }
      //}

      script {

        xInfo("curl: writing taskConfigureSource script..")
        println("""echo running configure at `date` ..""")
        println("if [ ! -f configure ]; then (autoreconf -fiv || exit 1); fi")
        println("if [ ! -f Makefile ]; then")
        println("./configure --prefix=\"${outputDirectory.get()}\" --host=${konanTarget.hostTriplet} \\")
        //println("--disable-tcl --disable-static --disable-readline")
        if (konanTarget == KonanTarget.LINUX_X64)
          println("--with-openssl=/files/cache/xtras/lib/openssl_linuxX64_3.6.1 \\")
        else if (konanTarget == KonanTarget.LINUX_ARM64)
          println("--with-openssl=/files/cache/xtras/lib/openssl_linuxArm64_3.6.1 \\")
        else if (konanTarget == KonanTarget.MACOS_X64)
          println("--with-openssl=/Users/dan/workspace/xtras/xtras/lib/openssl_macosX64_3.6.1 \\")
        else if (konanTarget == KonanTarget.MACOS_ARM64)
          println("--with-openssl=/Users/dan/workspace/xtras/xtras/lib/openssl_macosArm64_3.6.1 \\")
        println("--without-libpsl --without-ldap --without-libidn2")
        //if (konanTarget.family == Family.ANDROID) println("-D__ANDROID_API__=${xtras.android.sdkVersion.get()} \\")
        //println("no-engine no-asm no-tests threads zlib --prefix=\"${outputDirectory.get()}\" --libdir=lib")
        println("fi || exit 1")

        println("echo source configured .. building in 2")
        println("sleep 2")
        println("make || exit 1")
        println("make install")
      }
    }
  }
}

