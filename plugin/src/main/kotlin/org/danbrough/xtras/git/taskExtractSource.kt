package org.danbrough.xtras.git

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xWarn
import org.danbrough.xtras.xtrasSourceDownloadTaskName
import org.danbrough.xtras.xtrasSourceExtractTaskName
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


internal fun XtrasLibrary.registerGitSourceExtractTasks() {
  listOf(HostManager.host).forEach {
    registerGitSourceExtractTask(it)
  }
}

internal fun XtrasLibrary.registerGitSourceExtractTask(target: KonanTarget): String {
  val taskName = xtrasSourceExtractTaskName(target)

  project.run {
    tasks.register(taskName) {
      group = Tasks.XTRAS_TASK_GROUP


      val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
      val sourcesDir = srcDir(target).resolve(gitConfig.commit.get())
      val outputFile = sourcesDir.resolve(".cloned")
      outputs.file(outputFile)
      dependsOn(xtrasSourceDownloadTaskName())

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

