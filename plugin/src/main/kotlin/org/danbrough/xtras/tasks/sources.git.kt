package org.danbrough.xtras.tasks

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtraDSL
import org.danbrough.xtras.XtrasExtension
import org.danbrough.xtras.logDebug

import org.danbrough.xtras.xtrasDownloadsDir
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

internal data class GitSource(val url: String, val commit: String) : LibraryExtension.SourceConfig

@XtraDSL
fun LibraryExtension.gitSource(url: String, commit: String) {
  sourceConfig = GitSource(url, commit)
}

private fun LibraryExtension.registerGitTagsTask(){
  val config = sourceConfig as GitSource

  project.tasks.register<Exec>(xtrasTaskName("tags",this@registerGitTagsTask.name)) {
    commandLine(
      xtras.buildEnvironment.binaries.git,
      "ls-remote",
      "-q",
      "--refs",
      "-t",
      config.url
    )

    group = XTRAS_TASK_GROUP
    description = "Prints out the tags from the remote repository"
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
internal fun LibraryExtension.registerGitTasks() {
  val config = sourceConfig as GitSource
  project.logDebug("configureGitSource(): ${config.url} with ${config.commit}")

  val buildEnvironment = xtras.buildEnvironment

  registerGitTagsTask()

  val repoDir = project.xtrasDownloadsDir.resolve(name)
  val downloadSourceTaskName =  taskNameDownloadSource()
  val initTaskName = "${downloadSourceTaskName}_init"
  val remoteAddTaskName = "${downloadSourceTaskName}_remote_add"
  val fetchTaskName = "${downloadSourceTaskName}_fetch"
  val resetTaskName = "${downloadSourceTaskName}_reset"

  project.tasks.register(downloadSourceTaskName) {
    group = XTRAS_TASK_GROUP
    outputs.dir(repoDir)
    onlyIf {
      sourcesRequired.get()
    }
    dependsOn(resetTaskName)
  }

  gitTask(
    initTaskName,
    listOf("init", "--bare", repoDir.absolutePath)
  ) {
    onlyIf {
      sourcesRequired.get() &&
      !repoDir.exists()
    }
  }

  gitTask(remoteAddTaskName, listOf("remote", "add", "origin", config.url)) {
    dependsOn(initTaskName)
    workingDir(repoDir)
    onlyIf {
      sourcesRequired.get() &&
      repoDir.resolve("config").let { configFile ->
        configFile.exists() && !configFile.readText().contains(config.url)
      }
    }
  }


  gitTask(fetchTaskName, listOf("fetch", "--depth", "1", "origin", config.commit)) {
    dependsOn(remoteAddTaskName)
    workingDir(repoDir)
    val commitFile = repoDir.resolve("fetch_${config.commit}")
    outputs.file(commitFile)
    onlyIf {
      sourcesRequired.get() && !commitFile.exists()
    }
    doLast {
      commitFile.writeText(
        repoDir.resolve("FETCH_HEAD").bufferedReader().use {
          val commit = it.readLine().split("\\s+".toRegex(), limit = 2).first()
          project.logDebug("writing $commit to ${commitFile.absolutePath}")
          commit
        }
      )
    }
  }


  gitTask(resetTaskName) {
    inputs.property("config", config.hashCode())
    dependsOn(fetchTaskName)
    doFirst {
      val commit = repoDir.resolve("fetch_${config.commit}").readText()
      commandLine(buildEnvironment.binaries.git, "reset", "--soft", commit)
    }
    onlyIf {
      sourcesRequired.get()
    }
    workingDir(repoDir)
    outputs.dir(repoDir)
  }




  supportedTargets.get().forEach {target->
    val sourceDir = this@registerGitTasks.sourceDir(target)
    gitTask(
      taskNameExtractSource(target),
      listOf("clone", repoDir.absolutePath, sourceDir.absolutePath)
    ) {
      group = XTRAS_TASK_GROUP
      doFirst {
        sourceDir.parentFile.mkdirs()
      }
      onlyIf {
        !sourceDir.exists() && buildRequired.get().invoke(target)
      }
      description = "Extracts the sources for ${this@registerGitTasks.name} to ${sourceDir.absolutePath}"
      doFirst {
       // project.delete(sourceDir)
      }
      dependsOn(downloadSourceTaskName)
      //outputs.dir(sourceDir)
    }
  }
  //project.logWarn("TARGETS: ${project.kotlinExtension.targets.joinToString()}")

  /*
    supportedTargets.forEach { target ->
    val sourceDir = sourcesDir(target)
    gitTask(
      extractSourceTaskName(target),
      listOf("clone", repoDir.absolutePath, sourceDir.absolutePath)
    ) {
      group = XTRAS_TASK_GROUP
      doFirst {
        project.delete(sourceDir)
      }
      dependsOn(downloadSourcesTaskName)
      outputs.dir(sourceDir)
    }
  }
   */

}


private fun LibraryExtension.gitTask(
  name: String,
  args: List<String> = emptyList(),
  config: Exec.() -> Unit = {}
) =
  project.tasks.register<Exec>(name) {
    val buildEnvironment = xtras.buildEnvironment

    environment(buildEnvironment.getEnvironment())
    commandLine(args.toMutableList().apply {
      add(0, buildEnvironment.binaries.git)
    })
    doFirst {
      project.logDebug("running: ${commandLine.joinToString(" ")}")
    }
    config()
  }

