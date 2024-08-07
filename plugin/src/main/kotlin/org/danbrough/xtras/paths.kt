package org.danbrough.xtras

import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File


enum class XtrasPath {

  /**
   * Top level xtras directory where all other paths will reside by default
   */
  XTRAS,

  /**
   * Where binary archives are extract to for linking
   */
  LIBS,

  /**
   * Where compiled source is installed to
   */
  BUILD,

  /**
   * Where configure and build task output is written to
   */
  LOGS,

  /**
   * Where generated documentation is written to
   */
  DOCS,

  /**
   * Where source code is extracted to
   */
  SOURCE,

  /**
   * Where source archives and repos are downloaded to
   */
  DOWNLOADS,

  /**
   * Where binary archives are stored
   */
  PACKAGES,

  /**
   * Where the generated cinterop definition files are written to
   */
  INTEROPS,

  /**
   * Path of the local maven repository
   */
  MAVEN,

  /**
   * Path to the android ndk
   */
  NDK
  ;

  val propertyName: String
    get() = if (this == XTRAS) "xtras.dir" else "xtras.dir.${name.lowercase()}"

  override fun toString(): String = propertyName


}


/**
 * Path to the top level xtras directory.
 * This is where sources and binary packages are downloaded and built.
 *
 * Defaults to `project.rootProject.buildDir.resolve("xtras")`
 */

val Project.xtrasDir: File
  get() = xtrasPath(XtrasPath.XTRAS)

/**
 * Path to the xtras downloads directory.
 * This is where source archives are downloaded to.
 *
 * Defaults to `project.xtrasDir.resolve("downloads")`
 */
val Project.xtrasDownloadsDir: File
  get() = xtrasPath(XtrasPath.DOWNLOADS)


/**
 * Path to the xtras build directory.
 * This is the prefix directory for compiled source code.
 *
 * Defaults to `project.xtrasDir.resolve("build")`
 */
val Project.xtrasBuildDir: File
  get() = xtrasPath(XtrasPath.BUILD)


/**
 * Path to the xtras source directory.
 * This is where source code is located
 *
 * Defaults to `project.xtrasDir.resolve("src")`
 */
val Project.xtrasSourceDir: File
  get() = xtrasPath(XtrasPath.SOURCE)

/**
 * Path to the xtras packages directory.
 * This is where binary archives are stored.
 *
 * Defaults to `project.xtrasDir.resolve("packages")`
 */
val Project.xtrasPackagesDir: File
  get() = xtrasPath(XtrasPath.PACKAGES)


/**
 * Path to the xtras logs directory.
 * This is where log output of build tasks are stored.
 *
 * Defaults to `project.xtrasDir.resolve("logs")`
 */
val Project.xtrasLogsDir: File
  get() = xtrasPath(XtrasPath.LOGS)

/**
 * Path to the xtras maven directory.
 * This is where binary archives are published to.
 *
 * Defaults to `project.xtrasDir.resolve("maven")`
 */
val Project.xtrasMavenDir: File
  get() = xtrasPath(XtrasPath.MAVEN)


/**
 * Path to the xtras cinterops directory.
 * This is where cinterop files are generated from headers.
 *
 * Defaults to `project.xtrasDir.resolve("cinterops")`
 */
val Project.xtrasCInteropsDir: File
  get() = xtrasPath(XtrasPath.INTEROPS)


/**
 * Path to the xtras kdocs directory.
 * This is where kdoc documentation is generated to.
 *
 * Defaults to `project.xtrasDir.resolve("docs")`
 */
val Project.xtrasDocsDir: File
  get() = xtrasPath(XtrasPath.DOCS)


/**
 * Path to the xtras libs directory.
 * This where binary packages are extracted to.
 *
 * Defaults to `project.xtrasDir.resolve("libs")`
 */
val Project.xtrasLibsDir: File
  get() = xtrasPath(XtrasPath.LIBS)


/**
 * Path to the ndk directory.
 * Defaults to the environment variable ANDROID_NDK_ROOT then ANDROID_NDK_HOME
 */
val Project.xtrasNdkDir: File
  get() = xtrasPath(XtrasPath.NDK)


/**
 * Path to the msys directory (used on windows).
 */
val Project.xtrasMsysDir: File
  get() = projectProperty<File>("xtras.dir.msys") {
    File("C:/msys64")
  }

val Project.konanDir: File
  get() = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
    System.getProperty("user.home") ?: "NO_HOME",
    ".konan"
  )



