package org.danbrough.xtras

import java.io.File


fun pathOf(paths: Collection<Any?>): String =
  paths.mapNotNull {
    when (it) {
      is File -> it.absolutePath
      is Collection<*> -> pathOf(it).ifEmpty { null }
      else -> it?.toString()?.ifBlank { null }
    }
  }.joinToString(File.pathSeparator)

fun pathOf(vararg paths: Any?): String = pathOf(paths.toList())