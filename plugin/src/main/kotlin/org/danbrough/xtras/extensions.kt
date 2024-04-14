package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByName
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