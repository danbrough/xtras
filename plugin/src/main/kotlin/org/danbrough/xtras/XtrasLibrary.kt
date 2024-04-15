package org.danbrough.xtras

import org.danbrough.xtras.tasks.gitSource
import org.danbrough.xtras.tasks.registerTasks
import org.gradle.api.Project


@Suppress("MemberVisibilityCanBePrivate")
abstract class XtrasLibrary(
  val group: String,
  val name: String,
  val version: String,
  val xtras: XtrasExtension,
  val project: Project
) {
  internal interface SourceConfig

  internal var sourceConfig: SourceConfig? = null
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
