package org.danbrough.ssh2.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.XtrasPlugin
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.environmentApple
import org.danbrough.xtras.git.git
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.tasks.buildScript
import org.danbrough.xtras.xError
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasLibDir
import org.danbrough.xtras.xtrasName
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.Family


class SSH2Plugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.registerSsh2Library()
  }
}

private fun Project.registerSsh2Library() {
  println("registerSsh2Library: rootProject: ${rootProject.name} - xtras: ${rootProject.extensions.findByType<XtrasPlugin>()}")
  val xtrasEnv = xtras.environment
  val androidSdkVersion = xtras.android.sdkVersion.get()
  val xtrasLibDir = project.xtrasLibDir

  xtrasRegisterLibrary<XtrasLibrary>("ssh2") {
//    cinterops {
//      declaration {
//        println(
//          """
//        #staticLibraries =  libcrypto.a libssl.a
//        #headerFilter = openssl/**
//        #compilerOpts = -static
//
//
//        """.trimIndent()
//        )
//      }
//
//      extraCode {
//        println(
//          """
//            #include<stdio.h>
//            void testFunction(){
//              printf("Test SSH2 Function Works!!!\n");
//            }
//          """.trimIndent()
//        )
//      }
//    }

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
          xError("SETTING DEFAULT KONAN ENVIRONMENT")

          environment(xtrasEnv.androidEnvironment(env, target = konanTarget))
          env["CFLAGS"] = buildString {
            //        var cflags = "-Wno-unused-command-line-argument -Wno-macro-redefined -Os"
            append("-Wno-macro-redefined ")
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

        else -> {
          xError("SETTING DEFAULT KONAN ENVIRONMENT")
          environment(xtrasEnv.konanEnvironment(env, target = konanTarget))
        }
      }


      script {
        xInfo("ssh2: writing build script..")
        println("""echo running configure at `date` ..""")
        println("if [ ! -f configure ]; then (autoreconf -fiv || exit 1); fi")
        println("if [ ! -f Makefile ]; then")
        println("./configure --prefix=\"${outputDirectory.get()}\" --host=${konanTarget.hostTriplet} \\")
        println("--with-crypto=openssl --with-libssl-prefix=${xtrasLibDir.resolveAll("openssl_${konanTarget.xtrasName}_3.6.3/").absolutePath}   \\")
        println("--enable-static=no --disable-examples-build")
        println("fi || exit 1")
        println("echo source configured .. building in 2")
        println("sleep 2")
        println("make || exit 1")
        println("make install")
        //println("--with-openssl=${xtrasLibDir.resolveAll("openssl_${konanTarget.xtrasName}_3.6.1").absolutePath} \\")
        /*println("echo running configure at `date` ..")
        println("if [ ! -f Makefile ]; then")
        println("./Configure ${konanTarget.opensslPlatform} \\")
        if (konanTarget.family == Family.ANDROID) println("-D__ANDROID_API__=$androidSdkVersion \\")
        println("no-engine no-asm no-tests threads zlib --prefix=\"${outputDirectory.get()}\" --libdir=lib")
        println("fi || exit 1")

        println("echo source configured .. building in 2")
        println("sleep 2")
        println("make || exit 1")
        println("make install_sw")*/
      }
    }
  }
}


