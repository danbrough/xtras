package org.danbrough.xtras

import org.danbrough.xtras.tasks.CInteropsConfig
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.net.URI
import kotlin.reflect.KClass

class ScriptEnvironment(env: MutableMap<String, Any> = mutableMapOf()) :
  MutableMap<String, Any> by env

@XtrasDSL
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

  val interopsFile = project.objects.fileProperty().convention {
    project.xtrasBuildDir.resolve("interops")
      .resolve("${this@XtrasLibrary.name}_${version.get()}.def")
  }

  val buildEnabled =
    project.objects.property<(KonanTarget) -> Boolean>().convention(project.provider {
      { !packageFileMap.invoke(it).exists() }
    })


  var sourcesDirMap: (KonanTarget) -> File = {
    project.xtrasSrcDir.subPathMap(it)
  }

  var libDirMap: (KonanTarget) -> File = {
    project.xtrasLibDir.subPathMap(it)
  }

  var packageFileMap: (KonanTarget) -> File = {
    project.xtrasPackagesDir.resolveAll(
      name,
      group.get(),
      version.get(),
      it.xtrasName,
      "xtras_${name}_${version.get()}_${it.xtrasName}.tgz"
    )
  }

  var buildTargets: ListProperty<KonanTarget> =
    project.objects.listProperty<KonanTarget>().convention(project.provider {
      val kotlin = project.kotlinExtension
      if (kotlin is KotlinMultiplatformExtension) {
        kotlin.targets.filterIsInstance<KotlinNativeTarget>().map { it.konanTarget }
      } else emptyList()
    })

  @Optional
  internal val cinterops: Property<CInteropsConfig> = project.objects.property()

  override fun toString(): String = "$name:${version.get()}"
}


@XtrasDSL
inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, noinline block: T.() -> Unit = {}
): T = xtrasRegisterLibrary(name, block, T::class)

fun <T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, block: T.() -> Unit = {}, type: KClass<T>
): T {
  extensions.findByName(XTRAS_EXTN_NAME) ?: run {
    pluginManager.apply(XtrasPlugin::class.java)
  }

  val xtras =
    extensions.findByType<Xtras>() ?: error("Expecting Xtras extension to have been created")

  return extensions.create(name, type, xtras, this, name).apply(block)/*.also {
  xtrasConfigureLibrary(xtras, it)
}*/

}

/*
internal fun Project.xtrasConfigureLibrary(xtras: Xtras, library: XtrasLibrary) {
  xInfo("xtrasConfigureLibrary(): $library xtras:$xtras")
}
*/
