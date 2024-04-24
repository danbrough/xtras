package org.danbrough.xtras

import org.danbrough.xtras.tasks.gitSource
import org.danbrough.xtras.tasks.registerTasks
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

typealias TaskConfig = (KonanTarget) -> TaskProvider<*>

@Suppress("MemberVisibilityCanBePrivate")
@XtrasDSL
abstract class XtrasLibrary(
  val group: String,
  val name: String,
  val version: String,
  val xtras: XtrasExtension,
  val project: Project
) {
  internal interface SourceConfig

  internal var sourceConfig: SourceConfig? = null

  @XtrasDSL
  var buildEnabled: Boolean = project.projectProperty("${name}.buildEnabled") { false }

  internal var cinteropsConfig: CInteropsConfig? = null

  @XtrasDSL
  fun cinterops(block: CInteropsConfig.() -> Unit) {
    (cinteropsConfig ?: CInteropsConfig(
      defFile = localXtrasBuildDir.resolve("${name}_interops.def"),
      interopsPackage = "${project.group}.cinterops"
    ).also { cinteropsConfig = it }).block()
  }

  val downloadsDir: File
    get() = project.xtrasDownloadsDir.resolve(name)

  private val localXtrasBuildDir: File
    get() = project.layout.buildDirectory.asFile.get().resolve("xtras")

  private val localBuildDir: (dirName: String, target: KonanTarget) -> File = { dirName, target ->
    localXtrasBuildDir.resolveAll(dirName, name, version, target.kotlinTargetName)
  }

  @XtrasDSL
  var sourceDir: (KonanTarget) -> File = {
    localBuildDir("src", it)
  }

  @XtrasDSL
  var buildDir: (KonanTarget) -> File = {
    localBuildDir("build", it)
  }

  @XtrasDSL
  var libsDir: (KonanTarget) -> File = {
    project.xtrasLibsDir.resolve(name).resolve(version).resolve(it.kotlinTargetName)
  }

  @XtrasDSL
  var artifactID: (KonanTarget) -> String = {
    "binaries-${name}-${it.kotlinTargetName.lowercase()}"
  }

  @XtrasDSL
  var packageFile: (KonanTarget) -> File = { target ->
    val packageFileName =
      "xtras_${name}_${version}_${target.kotlinTargetName}.tgz"
    var file = project.xtrasPackagesDir
    group.split('.').forEach {
      file = file.resolve(it)
    }
    file.resolve(packageFileName)
  }

  internal var taskPrepareSource: TaskConfig? = null
  internal var taskConfigureSource: TaskConfig? = null
  internal var taskCompileSource: TaskConfig? = null
  internal var taskInstallSource: TaskConfig? = null

  internal var environment: XtrasEnvironmentConfig = {
    xtras.loadEnvironment(this, it)
  }

  @XtrasDSL
  fun environment(block: XtrasEnvironmentConfig) {
    environment.also { oldEnvironment ->
      environment = { target ->
        oldEnvironment(target)
        block(target)
      }
    }
  }

  fun loadEnvironment(env: XtrasEnvironment, target: KonanTarget): XtrasEnvironment =
    environment(env, target).let { env }

}

@XtrasDSL
inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  group: String,
  name: String,
  version: String,
  noinline block: T.() -> Unit = {}
): T = xtrasRegisterLibrary(group, name, version, T::class.java, block)


fun <T : XtrasLibrary> Project.xtrasRegisterLibrary(
  group: String,
  name: String,
  version: String,
  clazz: Class<T>,
  block: T.() -> Unit = {}
): T {

  extensions.findByName(name)?.also {
    error("Extension $name is already registered")
  }

  return extensions.create(name, clazz, group, name, version, project.xtras, this).also {
    extensions.configure<T>(name) {
      block()

      afterEvaluate {
        it.registerTasks()
        xtras.nativeTargets.get().forEach {
          //configureLinkTasks(it)
          registerBinaryPublication(it)
        }
      }

    }
  }
}

@XtrasDSL
inline fun <reified T : XtrasLibrary> Project.registerXtrasGitLibrary(
  extensionName: String,
  group: String = projectProperty<String>("$extensionName.group"),
  version: String = projectProperty<String>("$extensionName.version"),
  url: String = projectProperty<String>("$extensionName.url"),
  commit: String = projectProperty<String>("$extensionName.commit"),
  noinline block: T.() -> Unit
): T = extensions.findByName(extensionName)
  ?.let { error("extension $extensionName already registered: $it") } ?: xtrasRegisterLibrary<T>(
  group,
  extensionName,
  version
) {
  gitSource(url, commit)
  block()
}


/*

private fun XtrasLibrary.configureLinkTasks(target: KonanTarget){
  project.tasks.withType<KotlinNativeLink>{
    this.binary.linkTask.li
  }
}
*/
