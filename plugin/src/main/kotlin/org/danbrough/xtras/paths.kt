package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.internal.extensions.core.extra
import java.io.File


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


fun Project.xtrasPath(name: String? = null): File {
  val propertyName = if (name == null) PROPERTY_XTRAS_DIR else "$PROPERTY_XTRAS_DIR.$name"
  xDebug("Project.xtrasPath::getting $propertyName from extra")

  xDebug("project: $name hasProperty($propertyName) = ${hasProperty(propertyName)}")
  xDebug("project: $name extra.has($propertyName) = ${extra.has(propertyName)}")


  val pathValue = if (extra.has(propertyName)) extra[propertyName].toString() else null
  if (name == null && pathValue == null) error("$PROPERTY_XTRAS_DIR not set")
  if (pathValue != null) return File(pathValue)
  return File(xtrasPath(), name!!)
}


/**
 * Path to the xtras maven directory.
 * This is where binary archives are published to.
 *
 * Defaults to `project.xtrasDir.resolve("maven")`
 */
val Project.xtrasMavenDir: File
  get() = xtrasPath("maven")