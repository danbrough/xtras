package org.danbrough.xtras.git

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceDownload
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xWarn
import org.jetbrains.kotlin.konan.target.KonanTarget


internal fun XtrasLibrary.registerGitSourceExtractTask(target: KonanTarget): String {
  val taskName = taskNameSourceExtract(target)

  project.run {
    tasks.register(taskName) {
      group = TaskNames.XTRAS_TASK_GROUP


      //val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
      val sourcesDir = sourcesDirMap(target)
      val outputFile = sourcesDir.resolve(".xtras_extracted")

      outputs.file(outputFile)
      dependsOn(taskNameSourceDownload())

      description = "Download required commits from remote repository to $sourcesDir"

      onlyIf {
        !outputFile.exists()
      }

      actions.add {
        if (sourcesDir.exists()) {
          xWarn("deleting existing $sourcesDir")
          sourcesDir.deleteRecursively()
        }
        val cmdLine = listOf("git", "clone", cacheDir, sourcesDir)
        xTrace("running ${cmdLine.joinToString(" ")}")

        providers.exec {
          commandLine(*cmdLine.toTypedArray())
        }.also {
          xDebug("STDOUT: ${it.standardOutput.asText.get().trim()}")
          xDebug("STDERR: ${it.standardError.asText.get().trim()}")
          if (it.result.get().exitValue == 0) outputFile.createNewFile()
        }
      }


    }

  }
  return taskName
}

