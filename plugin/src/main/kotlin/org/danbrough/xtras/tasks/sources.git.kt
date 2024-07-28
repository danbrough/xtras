package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logError
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.logTrace
import org.danbrough.xtras.unixPath
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.environment
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
      enabled = buildEnabled || project.hasProperty("forceBuild")

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
            //xtrasCommandLine("git", "init", "--bare", repoDir)
            environment(loadEnvironment())
            logError("environment: $environment")
            commandLine(xtras.sh,"-c","git init --bare ${project.unixPath(repoDir)}")
            logTrace("running ${commandLine.joinToString(" ")}")
          }
          exec {
            workingDir(repoDir)
            environment(loadEnvironment())
            commandLine(xtras.sh,"-c","git remote add origin  ${config.url}")

            //xtrasCommandLine("git", "remote", "add", "origin", config.url)
            logTrace("running ${commandLine.joinToString(" ")}")
          }
        }
      }

      actions.add {
        exec {
          workingDir(repoDir)
          environment(loadEnvironment())
          commandLine(xtras.sh,"-c","git fetch --depth 1 origin ${config.commit}")
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
          environment(loadEnvironment())
          val commit = commitFile.readText()
          //xtrasCommandLine("git", "reset", "--soft", commit)
          commandLine(xtras.sh,"-c","git reset --soft $commit")

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
    enabled = buildEnabled || project.hasProperty("forceBuild")
    description =
      "Extracts the source code for ${this@registerSourceExtractTask.name} to $srcDir"
    inputs.property("commit", sourceConfig.hashCode())
    val commitFile = srcDir.resolve(".commit_${sourceConfig.hashCode()}")
    //inputs.dir(downloadsDir)
    outputs.file(commitFile)
    onlyIf { !packageFile(target).exists()  || project.hasProperty("forceBuild")}
    dependsOn(SourceTaskName.DOWNLOAD.taskName(this@registerSourceExtractTask))

    doFirst {
      if (srcDir.exists()) srcDir.deleteRecursively()
      project.logDebug("$taskName: cloning $sourceConfig to $srcDir")
    }

    //xtrasCommandLine("git", "clone", downloadsDir, srcDir)
    environment(loadEnvironment())
    commandLine(xtras.sh,"-c","git clone  ${project.unixPath(downloadsDir)} ${project.unixPath(srcDir)}")


    doLast {
      commitFile.createNewFile()
    }
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
    environment(loadEnvironment())

    commandLine(xtras.sh,"-c","git ls-remote -q --refs -t ${config.url}")

    /*
    xtrasCommandLine(
      "git",
      "ls-remote",
      "-q",
      "--refs",
      "-t",
      config.url
    )*/

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