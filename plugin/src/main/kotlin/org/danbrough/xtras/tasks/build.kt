package org.danbrough.xtras.tasks

import org.danbrough.xtras.ScriptCommandContext
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.konanDepsTaskName
import org.danbrough.xtras.unixPath
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.PrintWriter

fun XtrasLibrary.registerBuildTask(target: KonanTarget) {

  val srcDir = sourceDir(target)

  val xtrasDir =srcDir.resolve("xtras")



  val buildTaskName = SourceTaskName.BUILD.taskName(this, target)
  val genScriptTaskName = "${buildTaskName}_generate_script"
  val printScriptTaskName = "${buildTaskName}_print_script"

  val scriptsDir= xtrasDir.resolve("scripts")
  val logsDir= xtrasDir.resolve("logs")

  val logWriter by lazy {
    PrintWriter(logsDir.also { it.mkdirs() }.resolve("${buildTaskName}.log"))
  }

  val envFile = scriptsDir.resolve("env.sh")


  val buildScriptFile =
    scriptsDir.resolve("build.sh")


  project.tasks.register(genScriptTaskName) {
    //onlyIf { !packageFile(target).exists() || project.hasProperty("forceBuild") }

    dependsOn(SourceTaskName.EXTRACT.taskName(this@registerBuildTask, target))
    dependsOn(":${target.konanDepsTaskName}")
    
    doFirst {
      scriptsDir.mkdirs()
      envFile.printWriter().use { writer ->
        loadEnvironment(target).also { env ->
          env.keys.forEach { key ->
            writer.println("export $key=\"${env[key]}\"")
          }
        }
      }

      buildScriptFile.printWriter().use { writer ->
        writer.println("#!/bin/sh")
        writer.println()
        writer.println("source ${project.unixPath(envFile)}")

        buildCommand!!.invoke(ScriptCommandContext(writer), target)
      }
    }
    outputs.files(envFile,buildScriptFile)
  }

  project.tasks.register(printScriptTaskName){
    dependsOn(genScriptTaskName)
    doFirst {
      println(envFile.readText())
      println("#------------------------------------------------------")
      println(buildScriptFile.readText())
    }
  }

  project.tasks.register<Exec>(buildTaskName) {
    onlyIf { !packageFile(target).exists() || project.hasProperty("forceBuild") }
    dependsOn(genScriptTaskName)
    group = XTRAS_TASK_GROUP

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
      project.unixPath(buildScriptFile)
    )

    doLast {
      logWriter.close()
      workingDir.resolve("xtras").copyRecursively(buildDir(target).resolve("xtras"), true)
    }
  }


}
