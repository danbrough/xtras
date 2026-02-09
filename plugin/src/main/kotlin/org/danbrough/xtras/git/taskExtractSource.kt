package org.danbrough.xtras.git

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceDownload
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xWarn
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget


internal fun XtrasLibrary.registerGitSourceExtractTask(target: KonanTarget): String {
  val taskName = taskNameSourceExtract(target)
  //val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
  val sourcesDir = sourcesDirMap(target)
  val outputFile = sourcesDir.resolve(".xtras_extracted")

  project.tasks.register<Exec>(taskName) {
    group = TaskNames.XTRAS_TASK_GROUP

    outputs.dir(sourcesDir)
    dependsOn(taskNameSourceDownload())

    description = "Download required commits from remote repository to $sourcesDir"

    onlyIf {
      !outputFile.exists()
    }

    commandLine("git", "clone", cacheDir, sourcesDir)

    doFirst {
      if (sourcesDir.exists()) {
        xWarn("deleting existing $sourcesDir")
        sourcesDir.deleteRecursively()
      }
      xDebug("running ${commandLine.joinToString(" ")}")
    }


    /*project.providers.exec {
        commandLine(cmdLine)
      }.also {
        xDebug("STDOUT: ${it.standardOutput.asText.get().trim()}")
        xDebug("STDERR: ${it.standardError.asText.get().trim()}")
        if (it.result.get().exitValue == 0) outputFile.createNewFile()
      }*/

  }


  return taskName
}

