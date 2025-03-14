package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.net.URI

internal class GitSourceConfigImpl( library: XtrasLibrary) : XtrasLibrary.GitSourceConfig {

  override val url: Property<URI> = library.project.xtrasProperty("${library.name}.git.url")

  override val commit: Property<String> =
    library.project.xtrasProperty("${library.name}.git.commit")

  private val log = library.project.xtrasLogger

  override fun configureTasks() {
    log.info("configureTasks() ${url.get()}:${commit.get()}")

  }

}

fun XtrasLibrary.git(block:XtrasLibrary.GitSourceConfig.()->Unit){
  sourceConfig = GitSourceConfigImpl(this).apply(block)
}