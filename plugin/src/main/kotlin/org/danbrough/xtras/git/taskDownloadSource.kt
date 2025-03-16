package org.danbrough.xtras.git

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xError
import org.danbrough.xtras.xTrace
import org.danbrough.xtras.xtrasCacheDir
import org.danbrough.xtras.xtrasSourceDownloadTaskName
import java.io.File


internal fun XtrasLibrary.registerGitSourceDownloadTask(): String {
  val taskName = xtrasSourceDownloadTaskName()
  project.run {
    tasks.register(taskName) {
      group = Tasks.XTRAS_TASK_GROUP
      description = "Download required commits from remote repository to $xtrasCacheDir"

      val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
      val repoDir: File = this@registerGitSourceDownloadTask.cacheDir
      //val commitFile: File = repoDir.resolve(".commit")
      val fetchFile = repoDir.resolve("FETCH_HEAD")

      inputs.property("url", gitConfig.url)
      inputs.property("commit", gitConfig.commit)
      outputs.file(fetchFile)

      doFirst {
        xDebug("running $name with ${gitConfig.url.get()} commit: ${gitConfig.commit.get()} gitDir: $repoDir")
      }


      actions.add {
        if (!repoDir.resolve("HEAD").exists()) {
          xDebug("initializing bare repository at $repoDir ..")
          providers.exec {
            //commandLine(xtras.binaries.sh, "-c", "git init --bare ${repoDir.absolutePath}")
            commandLine("git", "init", "--bare", repoDir.absolutePath)
          }.also {
            if (it.result.get().exitValue != 0) xError(it.standardError.asText.get().trim())
            xDebug(it.standardOutput.asText.get().trim())
          }
          providers.exec {
            workingDir(repoDir)
            commandLine("git", "remote", "add", "origin", gitConfig.url.get())
          }.also {
            if (it.result.get().exitValue != 0) xError(it.standardError.asText.get().trim())
            xDebug(it.standardOutput.asText.get().trim())
          }
        }
      }

      actions.add {
        val cmdLine = listOf("git", "fetch", "origin", "--depth", "1", gitConfig.commit.get())
        xTrace("running ${cmdLine.joinToString(" ")}")

        providers.exec {
          workingDir(repoDir)
          commandLine(*cmdLine.toTypedArray())
        }.also {
          xError(it.standardError.asText.get().trim())
          xDebug(it.standardOutput.asText.get().trim())
        }
      }

      actions.add {
        val cmdLine =
          listOf(
            "git",
            "reset",
            "--soft",
            fetchFile.readLines().first().split("\\s+".toRegex()).first().trim()
          )
        xTrace("running ${cmdLine.joinToString(" ")}")

        providers.exec {
          workingDir(repoDir)
          commandLine(*cmdLine.toTypedArray())
        }.also {
          xError(it.standardError.asText.get().trim())
          xDebug(it.standardOutput.asText.get().trim())
        }
      }
    }
  }
  return taskName
}

