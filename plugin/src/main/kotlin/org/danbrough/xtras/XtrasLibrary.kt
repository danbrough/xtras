package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import java.net.URI


open class XtrasLibrary(val xtras: Xtras, val project: Project, val name: String) {

  interface SourceConfig {
    fun configureTasks()
  }

  interface GitSourceConfig : SourceConfig {
    val url: Property<URI>
    val commit: Property<String>
  }

  val version: Property<String> = project.xtrasProperty<String>("$name.version")

  val group: Property<String> =
    project.xtrasProperty<String>("$name.group") { project.group.toString() }

  var sourceConfig: SourceConfig? = null
}


inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, block: T.() -> Unit = {}
): T {
  //extensions.findByType<Xtras>() ?: pluginManager.apply(XtrasPlugin::class.java)
//  pluginManager.apply("org.danbrough.xtras")

  extensions.findByName(XTRAS_EXTN_NAME) ?: run {
    pluginManager.apply(XtrasPlugin::class.java)
  }

  val xtras = extensions.findByType<Xtras>()

  return extensions.create<T>(name, xtras!!, this, name).apply {
    block()
    project.afterEvaluate {
      sourceConfig?.configureTasks()
    }
  }


//  return extensions.create<T>(name, xtras, this, name).run {
//    block()
//    project.afterEvaluate {
//      sourceConfig?.configureTasks()
//    }
//  }
}


