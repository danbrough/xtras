package org.danbrough.xtras

import org.danbrough.xtras.tasks.gitSource
import org.gradle.api.Project

inline fun <reified T : LibraryExtension> Project.registerGitLibrary(
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
