package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.kotlin.dsl.environment
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.util.Locale


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
  .exec(arrayOf(cygpath, "-up", file.absolutePath)).inputStream.bufferedReader().readText()
else file.absolutePath

fun Project.pathOf(paths: List<Any?>): String =
  paths.filterNotNull()
    .joinToString(":") { if (it is File) unixPath(it) else if (it is List<*>) pathOf(it) else it.toString() }

fun Project.pathOf(vararg paths: Any?): String = pathOf(paths.toList())

fun File.resolveAll(vararg paths: String): File = resolveAll(paths.toList())

fun File.resolveAll(paths: List<String>): File =
  paths.fold(this) { file, path -> file.resolve(path) }

fun Project.kotlinBinaries(
  binariesFilter: (NativeBinary) -> Boolean = { it.buildType == NativeBuildType.DEBUG && it.target.konanTarget == HostManager.host }
): List<NativeBinary> =
  extensions.findByType<KotlinMultiplatformExtension>()?.targets?.withType<KotlinNativeTarget>()
    ?.flatMap { it.binaries }
    ?.filter(binariesFilter)
    ?: emptyList()

fun Executable.xtrasLibraryPath(): String =
  project.pathOf(
    project.xtrasExtension.ldLibraryPath(buildType),
    runTask!!.environment[HostManager.host.envLibraryPathName]
  )


val NativeBinary.jniLibsDir: File
  get() = project.file("src").resolveAll(
    if (buildType.debuggable) "debug" else "release",
    target.konanTarget.androidLibDir!!
  )

val NativeBinary.runsOnHost: Boolean
  get() = this.target.konanTarget == HostManager.host