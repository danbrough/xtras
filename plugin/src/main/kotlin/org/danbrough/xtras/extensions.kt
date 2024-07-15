package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.util.Locale

val Project.xtras: Xtras
  get() = rootProject.extensions.getByName<Xtras>(XTRAS_EXTENSION_NAME)

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
    .joinToString(":") { if (it is File) unixPath(it) else it.toString() }

fun Project.pathOf(vararg paths: Any?): String = pathOf(paths.toList())

fun File.resolveAll(vararg paths: String): File = resolveAll(paths.toList())

fun File.resolveAll(paths: List<String>): File =
  paths.fold(this) { file, path -> file.resolve(path) }


fun Project.xtrasSharedLibs(
  targetFilter: (KotlinNativeTarget) -> Boolean = { it.konanTarget == HostManager.host },
  binariesFilter: (NativeBinary) -> Boolean = { it.buildType == NativeBuildType.DEBUG }
) =
  (kotlinExtension as KotlinMultiplatformExtension).targets.withType<KotlinNativeTarget>()
    .filter(targetFilter)
    .flatMap { it.binaries }
    .filter(binariesFilter).filterIsInstance<SharedLibrary>()


