package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.net.URI


@Suppress("MemberVisibilityCanBePrivate")
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

  val cacheDir: File
    get() = project.xtrasCacheDir.resolve(name)

  val buildDir: File
    get() = project.xtrasBuildDir

  val srcDir: File
    get() = buildDir.resolve("src")

  fun srcDir(target: KonanTarget) = srcDir.resolve(target.xtrasName)

}


inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, block: T.() -> Unit = {}
): T {

  extensions.findByName(XTRAS_EXTN_NAME) ?: run {
    pluginManager.apply(XtrasPlugin::class.java)
  }

  val xtras =
    extensions.findByType<Xtras>() ?: error("Expecting Xtras extension to have been created")

  return extensions.create<T>(name, xtras, this, name).apply {
    block()
    project.afterEvaluate {
      sourceConfig?.configureTasks()
    }
  }
}


