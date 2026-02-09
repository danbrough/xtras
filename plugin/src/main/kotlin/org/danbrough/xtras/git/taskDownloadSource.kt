package org.danbrough.xtras.git

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceDownload
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register


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
  val repoDir = cacheDir
  val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
  val gitUrl = gitConfig.url
  val gitCommit = gitConfig.commit

  val writeScriptTaskName = "${taskName}_writeScript"
  val scriptFile = repoDir.resolve("download.sh")
  project.tasks.register(writeScriptTaskName) {
    inputs.property("url", gitUrl)
    inputs.property("commit", gitCommit)
    outputs.dir(repoDir)
    doFirst {
      if (!repoDir.exists())
        repoDir.mkdirs()
    }
    actions.add {
      scriptFile.printWriter().use { output ->
        output.println(
          """#!/usr/bin/env bash
cd ${repoDir.absolutePath}
if [ ! -f HEAD ]; then
  git init --bare .
  git remote add origin $gitUrl
  touch tags.txt
fi
if ! grep $gitCommit tags.txt ; then
  echo fetching $gitCommit from $gitUrl .. 
  git fetch origin --depth 1 $gitCommit || exit 1
  echo `cat FETCH_HEAD  | awk '{print $1}'`  $gitCommit >> tags.txt
fi
git reset --soft `grep $gitCommit tags.txt  | awk '{print $1}'`
            """.trim()
        )
      }
    }
  }

  project.tasks.register<Exec>(taskName) {
    dependsOn(writeScriptTaskName)
    group = TaskNames.XTRAS_TASK_GROUP
    inputs.property("url", gitUrl)
    inputs.property("commit", gitCommit)
    workingDir(repoDir)
    commandLine("bash", "download.sh")
    outputs.dir(repoDir)
  }
  return taskName
}
