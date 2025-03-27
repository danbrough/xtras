package org.danbrough.xtras.git

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xInfo
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayOutputStream

internal fun XtrasLibrary.taskNameGitSourceTags(): String = Tasks.create(
  Tasks.GROUP_SOURCE,
  "tags",
  this@taskNameGitSourceTags.name
)

internal fun XtrasLibrary.registerGitSourceTagsTask(): String {
  val taskName = taskNameGitSourceTags()
  project.run {
    tasks.register<Exec>(taskName) {
      group = Tasks.XTRAS_TASK_GROUP
      description = "List available ${this@registerGitSourceTagsTask.name} tags from git repo"
      doFirst {
        val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
        commandLine(
          xtras.binaries.bash.get(),
          "-c",
          "git ls-remote -q --refs -t ${gitConfig.url.get()}"
        )
      }

      val stdout = ByteArrayOutputStream()
      standardOutput = stdout
      doLast {
        stdout.toString().reader().use { reader ->
          reader.readLines().map { it ->
            it.split("\\s+".toRegex()).let {
              Pair(
                it[1].substringAfter("refs/tags/"),
                it[0]
              )
            }
          }.sortedBy { it.first }.forEach {
            xInfo("${it.first}\t${it.second}")
          }
        }
      }
    }
  }
  return taskName
}

