package org.danbrough.xtras

import org.gradle.api.provider.Property
import java.net.URI

internal class GitSourceConfigImpl(private val library: XtrasLibrary) :

  XtrasLibrary.GitSourceConfig {

  override val url: Property<URI> = library.project.xtrasProperty("${library.name}.git.url")

  override val commit: Property<String> =
    library.project.xtrasProperty("${library.name}.git.commit")

  private val log = library.project.xtrasLogger

  override fun configureTasks() {
    log.info("${library.project.path}:${library.name} configureTasks()")
    library.registerGitSourceDownloadTask()
  }

}

fun XtrasLibrary.git(block: XtrasLibrary.GitSourceConfig.() -> Unit) {
  sourceConfig = GitSourceConfigImpl(this).apply(block)
}

private fun XtrasLibrary.registerGitSourceDownloadTask() {
  project.run {
    tasks.register(Tasks.TASK_SOURCE_DOWNLOAD) {
      group = Tasks.XTRAS_TASK_GROUP
      description = "Download required commits from remote repository to $xtrasCacheDir"
      doFirst {
        val gitConfig = sourceConfig as XtrasLibrary.GitSourceConfig
        xInfo("running $name with ${gitConfig.url.get()} commit: ${gitConfig.commit.get()}")
      }
    }
  }
}