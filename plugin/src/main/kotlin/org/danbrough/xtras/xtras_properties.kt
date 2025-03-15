package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File

const val XTRAS_EXTN_NAME = "xtras"

const val PROPERTY_XTRAS_DIR = "$XTRAS_EXTN_NAME.dir"
const val PROPERTY_XTRAS_DOWNLOADS_DIR = "$PROPERTY_XTRAS_DIR.downloads"
const val PROPERTY_XTRAS_BUILD_DIR = "$PROPERTY_XTRAS_DIR.dir.build"

val Project.xtrasDir: File
  get() = getXtrasPropertyValue(PROPERTY_XTRAS_DIR) {
    rootDir.resolve("build").resolve(XTRAS_EXTN_NAME).absoluteFile
  }

val Settings.xtrasDir: File
  get() {
    val path = if (extraProperties.has(PROPERTY_XTRAS_DIR)) extraProperties.get(
      PROPERTY_XTRAS_DIR
    )!!.toString() else rootDir.resolve("build").resolve(XTRAS_EXTN_NAME).absolutePath
    return File(path)
  }

val Project.xtrasDownloadsDir: File
  get() = getXtrasPropertyValue(PROPERTY_XTRAS_DOWNLOADS_DIR) {
    xtrasDir.resolve("downloads")
  }

val Project.xtrasBuildDir: File
  get() = getXtrasPropertyValue(PROPERTY_XTRAS_BUILD_DIR) {
    layout.buildDirectory.dir(XTRAS_EXTN_NAME).get().asFile
  }


