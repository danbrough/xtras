package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.listProperty
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.net.URI

class ScriptEnvironment(env: MutableMap<String, Any> = mutableMapOf()) :
  MutableMap<String, Any> by env

@Suppress("MemberVisibilityCanBePrivate")
open class XtrasLibrary(val xtras: Xtras, val project: Project, val name: String) {

  interface SourceConfig

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

  var subPathMap: File.(KonanTarget) -> File = { target ->
    resolve("${this@XtrasLibrary.name}_${target.xtrasName}_${version.get()}")
  }

  var installDirMap: (KonanTarget) -> File = {
    project.xtrasBuildDir.subPathMap(it)
  }

  var sourcesDirMap: (KonanTarget) -> File = {
    project.xtrasSrcDir.subPathMap(it)
  }

  var packagesDirMap: (KonanTarget) -> File = {
    project.xtrasPackagesDir.subPathMap(it)
  }

  var scriptFileMap: (String, KonanTarget) -> File = { name, target ->
    sourcesDirMap(target).resolve("xtras_${name}_${target.xtrasName}.sh")
  }

  var buildTargets: ListProperty<KonanTarget> =
    project.objects.listProperty<KonanTarget>().convention(project.provider {
      val kotlin = project.kotlinExtension
      if (kotlin is KotlinMultiplatformExtension) {
        kotlin.targets.filterIsInstance<KotlinNativeTarget>().map { it.konanTarget }
      } else emptyList()
    })


}


inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, block: T.() -> Unit = {}
): T {

  extensions.findByName(XTRAS_EXTN_NAME) ?: run {
    pluginManager.apply(XtrasPlugin::class.java)
  }

  val xtras =
    extensions.findByType<Xtras>() ?: error("Expecting Xtras extension to have been created")

  return extensions.create<T>(name, xtras, this, name).apply(block)

}

