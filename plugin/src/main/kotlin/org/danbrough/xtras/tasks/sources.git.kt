package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.logTrace
import org.danbrough.xtras.xtrasDownloadsDir
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

internal data class GitSourceConfig(val url: String, val commit: String) :
  XtrasLibrary.SourceConfig

@XtrasDSL
fun XtrasLibrary.gitSource(url: String, commit: String) {
  sourceConfig = GitSourceConfig(url, commit)
}

fun XtrasLibrary.registerGitSourceTasks() {
  project.logInfo("$name::registerGitSourceTasks()")
  registerGitTagsTask()
  registerGitDownloadTask()
  registerGitExtractTask()
}

private fun XtrasLibrary.registerGitDownloadTask() {
  val config = sourceConfig as GitSourceConfig
  val downloadTaskName = SourceTaskName.DOWNLOAD.taskName(this)
  val repoDir = project.xtrasDownloadsDir.resolve(name)
  val commitFile = repoDir.resolve(".commit_${config.commit}")

  project.run {
    tasks.register(downloadTaskName) {
      group = XTRAS_TASK_GROUP
      description =
        "Downloads the source code from the remote repository ${config.url} with commit: ${config.commit}"
      outputs.file(commitFile)
      onlyIf {
        !commitFile.exists()
      }

      doFirst {
        logTrace("running $name repoDir: $repoDir exists: ${repoDir.exists()}")
      }

      if (!repoDir.resolve("HEAD").exists()) {
        actions.add {
          exec {
            xtrasCommandLine(xtras.tools.git, "init", "--bare", repoDir)
            logTrace("running ${commandLine.joinToString(" ")}")
          }
        }
        actions.add {
          exec {
            workingDir(repoDir)
            xtrasCommandLine(xtras.tools.git, "remote", "add", "origin", config.url)
            logTrace("running ${commandLine.joinToString(" ")}")
          }
        }
      }

      actions.add {
        exec {
          workingDir(repoDir)
          xtrasCommandLine(xtras.tools.git, "fetch", "--depth", "1", "origin", config.commit)
          logTrace("running: ${commandLine.joinToString(" ")}")
        }
      }

      doLast {
        repoDir.resolve("FETCH_HEAD").bufferedReader().use {
          val commit = it.readLine().split("\\s+".toRegex(), limit = 2).first()
          logDebug("writing $commit to ${commitFile.absolutePath}")
          commitFile.writer().use { writer ->
            writer.write(commit)
          }
        }
      }
    }
  }
}

private fun XtrasLibrary.registerGitExtractTask() {
  val config = sourceConfig as GitSourceConfig
  val downloadTaskName = SourceTaskName.EXTRACT.taskName(this)
  val repoDir = project.xtrasDownloadsDir.resolve(name)
  val commitFile = repoDir.resolve(".commit_${config.hashCode()}")
  project.rootProject.run {

  }
}

private fun XtrasLibrary.registerGitTagsTask() {
  val config = sourceConfig as GitSourceConfig
  val tagsTaskName = SourceTaskName.TAGS.taskName(this)
  //project.logTrace("registerGitTagsTask(): $tagsTaskName")
  project.rootProject.run {
    tasks.findByName(tagsTaskName) ?: tasks.register<Exec>(
      tagsTaskName
    ) {
      group = XTRAS_TASK_GROUP
      description = "Prints out the tags from the remote repository"

      xtrasCommandLine(
        xtras.tools.git,
        "ls-remote",
        "-q",
        "--refs",
        "-t",
        config.url
      )

      val stdout = ByteArrayOutputStream()
      standardOutput = stdout
      doLast {
        InputStreamReader(ByteArrayInputStream(stdout.toByteArray())).use { reader ->
          reader.readLines().map { it ->
            it.split("\\s+".toRegex()).let {
              Pair(
                it[1].substringAfter("refs/tags/"),
                it[0]
              )
            }
          }.sortedBy { it.first }.forEach {
            println("TAG: ${it.first}\t${it.second}")
          }
        }
      }
    }
  }
}