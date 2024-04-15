package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.logTrace
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
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
  registerDownloadTask()
  xtras.nativeTargets.get().forEach { registerSourceExtractTask(it) }

}

private fun XtrasLibrary.registerDownloadTask() {
  val config = sourceConfig as GitSourceConfig
  val downloadTaskName = SourceTaskName.DOWNLOAD.taskName(this)
  val repoDir = downloadsDir
  val commitFile = repoDir.resolve(".commit_${config.commit}")

  project.run {
    tasks.register(downloadTaskName) {
      group = XTRAS_TASK_GROUP
      description =
        "Downloads the source code from the remote repository ${config.url} with commit: ${config.commit}"

      inputs.property("commit", sourceConfig.hashCode())
      outputs.file(commitFile)

      doFirst {
        logTrace("running $name repoDir: $repoDir exists: ${repoDir.exists()}")
      }

      if (!repoDir.resolve("HEAD").exists()) {
        actions.add {
          exec {
            xtrasCommandLine(xtras.tools.git, "init", "--bare", repoDir)
            logTrace("running ${commandLine.joinToString(" ")}")
          }
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

        repoDir.resolve("FETCH_HEAD").bufferedReader().use {
          val commit = it.readLine().split("\\s+".toRegex(), limit = 2).first()
          logDebug("writing $commit to ${commitFile.absolutePath}")
          commitFile.writer().use { writer ->
            writer.write(commit)
          }
        }

        exec {
          workingDir(repoDir)
          val commit = commitFile.readText()
          xtrasCommandLine(xtras.tools.git, "reset", "--soft", commit)
          logTrace("running: ${commandLine.joinToString(" ")}")
        }
      }
    }
  }
}

private fun XtrasLibrary.registerSourceExtractTask(target: KonanTarget) {
  val taskName = SourceTaskName.EXTRACT.taskName(this, target)
  val srcDir = sourceDir(target)
  project.tasks.register<Exec>(taskName) {
    group = XTRAS_TASK_GROUP
    description = "Extracts the source code for ${this@registerSourceExtractTask.name} to $srcDir"
    inputs.property("commit", sourceConfig.hashCode())
    //inputs.dir(downloadsDir)
    outputs.dir(srcDir)
    dependsOn(SourceTaskName.DOWNLOAD.taskName(this@registerSourceExtractTask))
    doFirst {
      if (srcDir.exists()) srcDir.deleteRecursively()
      project.logDebug("$taskName: cloning $sourceConfig to $srcDir")
    }

    xtrasCommandLine(xtras.tools.git, "clone", downloadsDir, srcDir)
  }
}

private fun XtrasLibrary.registerGitTagsTask() {
  val config = sourceConfig as GitSourceConfig
  val tagsTaskName = SourceTaskName.TAGS.taskName(this)
  //project.logTrace("registerGitTagsTask(): $tagsTaskName")
  project.tasks.findByName(tagsTaskName) ?: project.tasks.register<Exec>(
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