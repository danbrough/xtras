package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File

const val XTRAS_EXTN_NAME = "xtras"

const val PROPERTY_XTRAS_DIR = "$XTRAS_EXTN_NAME.dir"
const val PROPERTY_XTRAS_CACHE_DIR = "$PROPERTY_XTRAS_DIR.cache"
const val PROPERTY_XTRAS_BUILD_DIR = "$PROPERTY_XTRAS_DIR.dir.build"
const val PROPERTY_XTRAS_SRC_DIR = "$PROPERTY_XTRAS_DIR.dir.src"

val Project.xtrasDir: File
  get() = xtrasPropertyValue(PROPERTY_XTRAS_DIR) {
    rootDir.resolve("build").resolve(XTRAS_EXTN_NAME).absoluteFile
  }

val Settings.xtrasDir: File
  get() {
    val path = if (extraProperties.has(PROPERTY_XTRAS_DIR)) extraProperties.get(
      PROPERTY_XTRAS_DIR
    )!!.toString() else rootDir.resolve("build").resolve(XTRAS_EXTN_NAME).absolutePath
    return File(path)
  }

val Project.xtrasCacheDir: File
  get() = xtrasPropertyValue(PROPERTY_XTRAS_CACHE_DIR) {
    xtrasDir.resolve("cache")
  }


val Project.xtrasBuildDir: File
  get() = xtrasPropertyValue(PROPERTY_XTRAS_BUILD_DIR) {
    xtrasDir.resolve("build")
    //layout.buildDirectory.dir(XTRAS_EXTN_NAME).get().asFile
  }


val Project.xtrasSrcDir: File
  get() = xtrasPropertyValue(PROPERTY_XTRAS_SRC_DIR) {
    //layout.buildDirectory.dir(XTRAS_EXTN_NAME).get().asFile
    xtrasDir.resolve("src")
  }

val Project.xtrasKonanDir: File
  get() = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
    System.getProperty("user.home")!!,
    ".konan"
  )



