package org.danbrough.xtras

import org.danbrough.xtras.tasks.gitSource
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType

inline fun <reified T : LibraryExtension> Project.registerGitLibrary(
	extensionName: String,
	group: String = projectProperty<String>("$extensionName.group"),
	version: String = projectProperty<String>("$extensionName.version"),
	url: String = projectProperty<String>("$extensionName.url"),
	commit: String = projectProperty<String>("$extensionName.commit"),
	noinline block: T.() -> Unit
): T {
	val extn = extensions.findByName(extensionName)
	if (extn != null)
		error("extension $extensionName already registered: $extn")
	return xtrasRegisterLibrary<T>(group, extensionName, version) {
		gitSource(url, commit)
		block()
	}
}