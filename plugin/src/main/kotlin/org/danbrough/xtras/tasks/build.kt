package org.danbrough.xtras.tasks

import org.danbrough.xtras.ScriptCommandContext
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.logError
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.unixPath
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter
import java.util.Date

fun XtrasLibrary.registerBuildTask(target: KonanTarget) {

  val srcDir = sourceDir(target)

  val xtrasDir =srcDir.resolve("xtras")



  val buildTaskName = SourceTaskName.BUILD.taskName(this, target)
  val genEnvTaskName = "${buildTaskName}_env"

  val scriptsDir= xtrasDir.resolve("scripts")
  val logsDir= xtrasDir.resolve("logs")

  val logWriter by lazy {
    PrintWriter(logsDir.also { it.mkdirs() }.resolve("${buildTaskName}.log"))
  }

  val envFile = scriptsDir.resolve("env.sh")


  val buildScript =
    scriptsDir.resolve("build.sh")


  project.tasks.register(genEnvTaskName) {
    onlyIf { !packageFile(target).exists() }

    dependsOn(SourceTaskName.EXTRACT.taskName(this@registerBuildTask, target))
    doFirst {
      scriptsDir.mkdirs()
      envFile.printWriter().use { writer ->
        loadEnvironment(target).also { env ->
          env.keys.forEach { key ->
            writer.println("export $key=\"${env[key]}\"")
          }
        }
      }


      buildScript.printWriter().use { writer ->
        writer.println("#!/bin/sh")
        writer.println()
        writer.println("source ${project.unixPath(envFile)}")

        buildCommand!!.invoke(ScriptCommandContext(writer), target)
      }
    }
    //outputs.file(envFile)
  }

  project.tasks.register<Exec>(buildTaskName) {
    onlyIf { !packageFile(target).exists() }
    dependsOn(genEnvTaskName)

    //environment(loadEnvironment(target))

    workingDir = srcDir


    doFirst {
      logWriter.println("# ${this@register.name}: running ${commandLine.joinToString(" ")}")
      logWriter.println()
    }

    processStdout {
      println(it)
      logWriter.println(it)
      logWriter.flush()
    }
    processStderr {
      println("ERROR: $it")
      logWriter.println("ERROR: $it")
      logWriter.flush()
    }

    commandLine(
      xtras.sh,
      project.unixPath(buildScript)
    )

    doLast {
      logWriter.close()
      workingDir.resolve("xtras").copyRecursively(buildDir(target).resolve("xtras"), true)
    }
  }
}
