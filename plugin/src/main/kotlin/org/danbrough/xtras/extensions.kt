package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByName
import org.gradle.process.ExecSpec
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.util.Locale

val Project.xtras: XtrasExtension
	get() = project.extensions.getByName<XtrasExtension>(XTRAS_EXTENSION_NAME)

fun Project.xtrasPath(path: XtrasPath): File {
	//println("Project.xtrasPath::getting ${path.propertyName} from extra")
	val pathValue = if (extra.has(path.propertyName)) extra[path.propertyName]?.toString() else null

	return if (pathValue == null) if (path == XtrasPath.XTRAS) error("${XtrasPath.XTRAS} not set")
	else xtrasPath(XtrasPath.XTRAS).resolve(path.name.lowercase())
	else File(pathValue)
}


fun String.capitalized() =
	replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.decapitalized() = replaceFirstChar { it.lowercase(Locale.getDefault()) }

fun ExecSpec.xtrasCommandLine(vararg args: Any) {
	commandLine(args.toList())
}

fun ExecSpec.xtrasCommandLine(args: Iterable<Any>) {
	commandLine(args.map { if (it is File) it.mixedPath else it })
}

private val hostIsMingw = HostManager.hostIsMingw

val File.mixedPath: String
	get() = absolutePath.let {
		if (hostIsMingw) it.replace("\\", "/") else it
	}

private val Project.cygpath: String
	get() = projectProperty("xtras.cygpath") {
		xtrasMsysDir.resolveAll("usr", "bin", "cygpath.exe").absolutePath
	}

fun Project.unixPath(file: File): String = if (HostManager.hostIsMingw) Runtime.getRuntime()
	.exec(arrayOf(cygpath, "-u", file.absolutePath)).inputStream.readAllBytes()
	.decodeToString().trim()
else file.absolutePath

fun Project.pathOf(paths: List<Any?>): String =
	paths.filterNotNull()
		.joinToString(File.pathSeparator) { if (it is File) unixPath(it) else it.toString() }

fun Project.pathOf(vararg paths: Any?): String = pathOf(paths.toList())

fun File.resolveAll(vararg paths: String): File = resolveAll(paths.toList())

fun File.resolveAll(paths: List<String>): File =
	paths.fold(this) { file, path -> file.resolve(path) }