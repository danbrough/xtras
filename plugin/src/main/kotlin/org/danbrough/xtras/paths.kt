package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.extra
import java.io.File

const val XTRAS_PATH = "xtras.dir"

fun pathOf(paths: Collection<Any?>): String =
  paths.mapNotNull {
    when (it) {
      is File -> it.absolutePath
      is Property<*> -> pathOf(it.get())
      is Collection<*> -> pathOf(it).ifEmpty { null }
      else -> it?.toString()?.ifBlank { null }
    }
  }.joinToString(File.pathSeparator)

fun pathOf(vararg paths: Any?): String = pathOf(paths.toList())


fun File.resolveAll(vararg paths: String): File = resolveAll(paths.toList())

fun File.resolveAll(paths: List<String>): File =
  paths.fold(this) { file, path -> file.resolve(path) }


fun Project.xtrasPath(path: String): File {
  //println("Project.xtrasPath::getting ${path.propertyName} from extra")
  val pathValue = if (extra.has(path)) extra[path]?.toString() else null

  return if (pathValue == null) if (path == XTRAS_PATH) error("$XTRAS_PATH not set")
  else xtrasPath(XTRAS_PATH).resolve(path)
  else File(pathValue)
}


/**
 * Path to the xtras maven directory.
 * This is where binary archives are published to.
 *
 * Defaults to `project.xtrasDir.resolve("maven")`
 */
val Project.xtrasMavenDir: File
  get() = xtrasPath("$XTRAS_PATH.maven")