package org.danbrough.xtras.git

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceDownload
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xtrasCacheDir
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.File


/*
abstract class SourceDownload : DefaultTask() {


  @get:Input
  abstract val sourceConfig: Property<XtrasLibrary.GitSourceConfig>

  // Execution code
  @TaskAction
  fun print() {
    println("Source config: ${sourceConfig.get()}")
  }
}
*/


internal fun XtrasLibrary.registerGitSourceDownloadTask(): String {
  val taskName = taskNameSourceDownload()
  val repoDir: File = cacheDir
  val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
  val gitUrl = gitConfig.url
  val gitCommit = gitConfig.commit
  val fetchFile = repoDir.resolve(".commit")
  val originFile = repoDir.resolve(".origin")


  val initTaskName = "${taskName}_init"
  project.tasks.register<Exec>(initTaskName) {
    doFirst {
      xInfo("initializing bare repository at $repoDir ..")
    }
    commandLine("git", "init", "--bare", repoDir.absolutePath)
    onlyIf {
      !repoDir.resolve("HEAD").exists()
    }
  }

  val remoteAddOriginTaskName = "${taskName}_addOrigin"
  project.tasks.register<Exec>(remoteAddOriginTaskName) {
    doFirst {
      xInfo("Adding remote $gitUrl ..")
    }
    workingDir(repoDir)
    dependsOn(initTaskName)
    inputs.property("url", gitUrl)
    commandLine("git", "remote", "add", "origin", gitUrl)
    outputs.file(originFile)

    onlyIf {
      !originFile.exists() || !originFile.readText().contains(gitUrl.toString())
    }

    doLast {
      originFile.writeText(gitUrl.toString())
    }
  }

  //val cmdLine = listOf("git", "fetch", "origin", "--depth", "1", gitConfig.commit.get())
  val gitFetchTaskName = "${taskName}_fetch"
  project.tasks.register<Exec>(gitFetchTaskName) {
    doFirst {
      xInfo("Fetching $gitCommit from $gitUrl ..")
    }
    workingDir(repoDir)
    dependsOn(remoteAddOriginTaskName)
    inputs.property("url", gitUrl)
    inputs.property("commit", gitCommit)
    outputs.file(fetchFile)

    commandLine("git", "fetch", "origin", "--depth", "1", gitCommit)

    doLast {
      repoDir.resolve("FETCH_HEAD").bufferedReader().use {
        it.readLine().split("\\s+".toRegex(), limit = 2).first()
      }.also { commit ->
        xDebug("writing $commit to ${fetchFile.absolutePath}")
        fetchFile.writeText(commit)
      }
    }
  }.get().outputs.files.first()

  val gitResetTaskName = "${taskName}_reset"
  project.tasks.register<Exec>(gitResetTaskName) {
    dependsOn(gitFetchTaskName)
    workingDir(repoDir)
    inputs.file(fetchFile)
    doFirst {
      commandLine(
        "git",
        "reset",
        "--soft",
        fetchFile.readLines().first().split("\\s+".toRegex()).first().trim()
      )
      xInfo("running $name .. $commandLine")
    }
  }

  project.tasks.register(taskName) {
    dependsOn(gitResetTaskName)
    group = TaskNames.XTRAS_TASK_GROUP
    description = "Download required commits from remote repository to ${project.xtrasCacheDir}"


    //val commitFile: File = repoDir.resolve(".commit")
    val fetchFile = repoDir.resolve("FETCH_HEAD")
    inputs.property("url", gitUrl)
    inputs.property("commit", gitCommit)
    outputs.file(fetchFile)

    doFirst {
      xInfo("running $name with ${inputs.properties["url"]} commit: ${inputs.properties["commit"]} gitDir: $repoDir")
    }


    /*    actions.add {
          if (!repoDir.resolve("HEAD").exists()) {
            xInfo("initializing bare repository at $repoDir ..")
            projectProviders.exec {
              //commandLine(xtras.binaries.sh, "-c", "git init --bare ${repoDir.absolutePath}")
              commandLine("git", "init", "--bare", repoDir.absolutePath)
            }.also {
              if (it.result.get().exitValue != 0) xError(it.standardError.asText.get().trim())
              xDebug(it.standardOutput.asText.get().trim())
            }

            projectProviders.exec {
              workingDir(repoDir)
              commandLine("git", "remote", "add", "origin", gitUrl)
            }.also {
              if (it.result.get().exitValue != 0) xError(it.standardError.asText.get().trim())
              xDebug(it.standardOutput.asText.get().trim())
            }
          }

          val cmdLine = listOf("git", "fetch", "origin", "--depth", "1", gitConfig.commit.get())
          xDebug("running ${cmdLine.joinToString(" ")}")

          projectProviders.exec {
            workingDir(repoDir)
            commandLine(cmdLine)
          }.also {
            xError(it.standardError.asText.get().trim())
            xInfo(it.standardOutput.asText.get().trim())
          }

          repoDir.resolve("FETCH_HEAD").bufferedReader().use {
            val commit = it.readLine().split("\\s+".toRegex(), limit = 2).first()
            xDebug("writing $commit to ${fetchFile.absolutePath}")
            fetchFile.writer().use { writer ->
              writer.write(commit)
            }
          }
        }*/

    /*

          val cmdLine2 = listOf(
            "git",
            "reset",
            "--soft",
            fetchFile.readLines().first().split("\\s+".toRegex()).first().trim()
          )
          xDebug("running ${cmdLine2.joinToString(" ")}")

          projectProviders.exec {
            workingDir(repoDir)
            commandLine(cmdLine2)
          }.also {
            xError(it.standardError.asText.get().trim())
            xInfo(it.standardOutput.asText.get().trim())
          }
        }*/
  }

  return taskName
}

