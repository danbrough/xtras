package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File

internal const val XTRAS_PROPERTY = "xtras"

const val PROPERTY_XTRAS_DIR = "$XTRAS_PROPERTY.dir"

val Project.xtrasDir: File
  get() = getXtrasPropertyValue(PROPERTY_XTRAS_DIR) {
    val xtrasDir = rootProject.layout.buildDirectory.dir("xtras").get().asFile
    xWarn("$PROPERTY_XTRAS_DIR not set. Using default: ${xtrasDir.absolutePath}")
    xtrasDir
  }

val Settings.xtrasDir: File
  get() {
    val path = if (extraProperties.has(PROPERTY_XTRAS_DIR)) extraProperties.get(
      PROPERTY_XTRAS_DIR
    )!!.toString() else rootDir.resolve("build").resolve("xtras").absolutePath
    return File(path)
  }



