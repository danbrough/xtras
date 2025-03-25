package org.danbrough.xtras

import org.gradle.api.provider.Property
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