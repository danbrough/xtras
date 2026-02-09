package org.danbrough.xtras.git

import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xtrasProperty
import java.net.URI
import java.net.URL

internal class GitSourceConfigImpl(private val library: XtrasLibrary) :
  XtrasLibrary.GitSourceConfig {

  override val url: URL =
    library.project.xtrasProperty<URI>("${library.name}.git.url").get().toURL()

  override val commit: String =
    library.project.xtrasProperty<String>("${library.name}.git.commit").get()

}

@XtrasDSL
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
  project.afterEvaluate {
    registerGitSourceTagsTask()
    registerGitSourceDownloadTask()
    buildTargets.get().forEach { target ->
      registerGitSourceExtractTask(target)
    }
  }

}

