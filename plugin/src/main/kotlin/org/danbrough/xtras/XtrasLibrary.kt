package org.danbrough.xtras

import org.danbrough.xtras.tasks.gitSource
import org.danbrough.xtras.tasks.registerTasks
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


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

  val downloadsDir: File
    get() = project.xtrasDownloadsDir.resolve(name)

  private val localXtrasBuildDir: File = project.layout.buildDirectory.asFile.get().resolve("xtras")

  private val localBuildDir: (dirName: String, target: KonanTarget) -> File = { dirName, target ->
    localXtrasBuildDir.resolve(dirName).resolve(name).resolve(version)
      .resolve(target.kotlinTargetName)
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
        //TODO: it.registerPublications()
      }

    }
  }
}

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
