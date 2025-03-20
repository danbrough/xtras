package org.danbrough.xtras.git

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xtrasProperty
import org.gradle.api.provider.Property
import java.net.URI

internal class GitSourceConfigImpl(private val library: XtrasLibrary) :
  XtrasLibrary.GitSourceConfig {

  override val url: Property<URI> = library.project.xtrasProperty("${library.name}.git.url")

  override val commit: Property<String> =
    library.project.xtrasProperty("${library.name}.git.commit")

}

fun XtrasLibrary.git(block: XtrasLibrary.GitSourceConfig.() -> Unit) {
  val gitConfig = GitSourceConfigImpl(this).apply(block)
  sourceConfig = gitConfig

  /*
    //use a subfolder of the default sourcesDir
    val defaultSourcesDir = sourcesDirMap
    sourcesDirMap = { target ->
      defaultSourcesDir(target).resolve(gitConfig.commit.get())
    }
  */

  //register the git related source tasks
  registerGitSourceTagsTask()
  registerGitSourceDownloadTask()
  registerGitSourceExtractTasks()

}

