package org.danbrough.xtras

import org.danbrough.xtras.tasks.defaultCInteropsTargetWriter
import org.danbrough.xtras.tasks.registerTasks
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias TaskConfig = (KonanTarget) -> TaskProvider<*>
typealias CInteropsTargetWriter = LibraryExtension.(KonanTarget, PrintWriter) -> Unit

//val thang: ()->List<KonanTarget> = { listOf(KonanTarget.LINUX_ARM64,KonanTarget.LINUX_X64) }
@Suppress("MemberVisibilityCanBePrivate")
abstract class LibraryExtension(
  val group: String,
  val name: String,
  val version: String,
  val project: Project
) {
  interface SourceConfig

  var sourceConfig: SourceConfig? = null

  var buildEnabled: Boolean = false

  var publishing: Boolean = false

  internal var taskPrepareSource: TaskConfig? = null
  internal var taskConfigureSource: TaskConfig? = null
  internal var taskCompileSource: TaskConfig? = null
  internal var taskInstallSource: TaskConfig? = null

  internal var dependencies = mutableListOf<LibraryExtension>()

  @XtraDSL
  fun dependsOn(vararg libs: LibraryExtension) {
    dependencies.addAll(libs)
  }

//	@XtraDSL
//	abstract val sourcesRequired: Property<Boolean>

//	/**
//	 * Whether source and build tasks are enabled.
//	 * By default this will be the value of the gradle property [name].buildRequired (true|false)
//	 * otherwise it will be true if the [packageFile] doesn't exist.
//	 */
//	@XtraDSL
//	abstract val buildRequired: Property<KonanTarget.() -> Boolean>

  @XtraDSL
  abstract val supportedTargets: ListProperty<KonanTarget>

  @XtraDSL
  var sourceDir: (KonanTarget) -> File = {
    project.xtrasSourceDir
      .resolve(name).resolve(it.platformName).resolve(version)
  }

  @XtraDSL
  var buildDir: (KonanTarget) -> File = {
    project.xtrasBuildDir
      .resolve(name).resolve(it.platformName).resolve(version)
  }

  @XtraDSL
  var packageFile: (KonanTarget) -> File = {
    project.xtrasPackagesDir.resolve(group.replace('.', File.separatorChar))
      .resolve("xtras_${name}_${it.platformName}_${version}.tgz")
  }


  @XtraDSL
  var artifactName: (KonanTarget) -> String = {
    "package-${name.lowercase()}-${it.platformName.lowercase()}"
  }

  @XtraDSL
  var libsDir: (KonanTarget) -> File = {
    project.xtrasLibsDir
      .resolve(name).resolve(it.platformName).resolve(version)
  }


  internal val cinteropsConfig = CInteropsConfig(
    "$group.cinterops",
    project.layout.buildDirectory.get().asFile.resolve(
      "generated/cinterops/${
        group.replace(
          '.',
          '_'
        )
      }-${name}_${version}.def"
    )
  )

  @XtraDSL
  fun cinterops(block: CInteropsConfig.() -> Unit) {
    cinteropsConfig.block()
  }

  override fun toString(): String =
    "${this::class.java.simpleName.substringBefore("_Decorated")}[$name:$version]"

  val xtras: XtrasExtension
      by lazy { project.extensions.findByType<XtrasExtension>()!! }

}


@XtraDSL
inline fun <reified T : LibraryExtension> Project.xtrasRegisterLibrary(
  group: String,
  name: String,
  version: String,
  noinline block: T.() -> Unit = {}
): T = xtrasRegisterLibrary(group, name, version, T::class.java, block)


fun <T : LibraryExtension> Project.xtrasRegisterLibrary(
  group: String,
  name: String,
  version: String,
  clazz: Class<T>,
  block: T.() -> Unit = {}
): T {

  extensions.findByName(name)?.also {
    error("Extension $name is already registered")
  }

  return extensions.create(name, clazz, group, name, version, this).also {
    extensions.configure<T>(name) {
      block()
      afterEvaluate {
        it.registerTasks()
        it.registerPublications()
      }
    }
  }
}


