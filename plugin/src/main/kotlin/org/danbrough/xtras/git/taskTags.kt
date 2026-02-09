package org.danbrough.xtras.git

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xInfo
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

internal fun XtrasLibrary.taskNameGitSourceTags(): String = TaskNames.create(
  TaskNames.GROUP_SOURCE, "tags", this@taskNameGitSourceTags.name
)

internal fun XtrasLibrary.registerGitSourceTagsTask(): String {
  val taskName = taskNameGitSourceTags()
  val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig

  project.tasks.register<Exec>(taskName) {
    group = TaskNames.XTRAS_TASK_GROUP
    description = "List available tags from git repo"

    commandLine(
      //xtras.binaries.bash.get(), "-c", "git ls-remote -q --refs -t ${gitConfig.url}"
      "git", "ls-remote", "-q", "--refs", "-t", gitConfig.url.toString()
    )
    doFirst {
      xInfo("running $name [${commandLine.joinToString { " " }}]")
    }


    /*    val stdout = ByteArrayOutputStream()
        standardOutput = stdout
        doLast {
          stdout.toString().reader().use { reader ->
            reader.readLines().map { it ->
              it.split("\\s+".toRegex()).let {
                Pair(
                  it[1].substringAfter("refs/tags/"), it[0]
                )
              }
            }.sortedBy { it.first }.forEach {
              xInfo("${it.first}\t${it.second}")
            }
          }
        }*/
  }

  return taskName
}

