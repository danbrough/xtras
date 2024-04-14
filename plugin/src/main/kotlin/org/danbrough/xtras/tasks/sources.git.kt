package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibraryExtension
import org.danbrough.xtras.logInfo
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

internal data class GitSourceConfig(val url: String, val commit: String) :
  XtrasLibraryExtension.SourceConfig

@XtrasDSL
fun XtrasLibraryExtension.gitSource(url: String, commit: String) {
  sourceConfig = GitSourceConfig(url, commit)
}

fun XtrasLibraryExtension.registerGitSourceTasks() {
  project.logInfo("$name::registerGitSourceTasks()")
  registerGitTagsTask()
}


private fun XtrasLibraryExtension.registerGitTagsTask() {
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