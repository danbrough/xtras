package org.danbrough.sqlite.plugin

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.androidEnvironment
import org.danbrough.xtras.environmentApple
import org.danbrough.xtras.git.git
import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.tasks.buildScript
import org.danbrough.xtras.tasks.cinterops
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family


class SQLitePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.registerSqliteLibrary()
  }
}

private fun Project.registerSqliteLibrary() {
  val androidSdkVersion = xtras.android.sdkVersion.get()
  val xtrasEnv = xtras.environment

  xtrasRegisterLibrary<XtrasLibrary>("sqlite") {
    cinterops {
      declaration {
        println(
          """
        #headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
        headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
        linkerOpts = -lssh2
        #staticLibraries =  libcrypto.a libssl.a
        #headerFilter = openssl/**
        #headers = sqlite3.h
        #excludeDependentModules = true
        #linkerOpts.linux = -ldl -lc -lm -lsqlite3 
        #linkerOpts.android = -ldl -lc -lm -lsqlite3
        #linkerOpts.macos = -ldl -lc -lm -lsqlite3
        #linkerOpts.ios = -ldl -lc -lm -lsqlite3
        #linkerOpts.mingw = -ldl -lc -lm -lsqlite3
        #compilerOpts.android = -D__ANDROID_API__=$androidSdkVersion  
        #compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        #compilerOpts = -static
       
        """.trimIndent()
        )
      }

      extraCode {
        println(
          """
            #include<stdio.h>
            void testFunction(){
              printf("Sqlite Test Function Works!!!\n");
            }
          """.trimIndent()
        )
      }
    }

    git {
      xTrace("configuring git for $name url:$url commit:$commit")
    }


    buildScript {
      //outputs.file(workingDir.resolve("Makefile"))
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

        else -> environment(xtrasEnv.konanEnvironment(env, target = konanTarget))
      }
      //}

      script {
        xInfo("ssh2: writing taskConfigureSource script..")
        println("echo running configure at `date` ..")
        println("if [ ! -f Makefile ]; then")
        println("./configure --prefix=\"${outputDirectory.get()}\"")
        //println("--disable-tcl --disable-static --disable-readline")
        //println("--disable-tcl --disable-static --disable-readline")/*println("./Configure ${konanTarget.opensslPlatform} \\")
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

