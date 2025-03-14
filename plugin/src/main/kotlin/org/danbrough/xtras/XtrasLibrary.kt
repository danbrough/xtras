package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import java.net.URI


open class XtrasLibrary(val project: Project, val name: String) {

  interface SourceConfig {
    fun configureTasks()
  }

  interface GitSourceConfig : SourceConfig {
    val url: Property<URI>
    val commit: Property<String>
  }

  val version: Property<String> =
    project.xtrasProperty<String>("$name.version")

  val group: Property<String> =
    project.xtrasProperty<String>("$name.group") { project.group.toString() }

  var sourceConfig: SourceConfig? = null
}


inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String,
  block: T.() -> Unit = {}
) = extensions.create<T>(name, this, name).run {
  block()
  project.afterEvaluate {
    sourceConfig?.configureTasks()
  }
}


