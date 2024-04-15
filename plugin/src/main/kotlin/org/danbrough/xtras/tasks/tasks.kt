package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.logDebug
import org.gradle.process.ExecSpec
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


enum class TaskGroup {
  SOURCE, PACKAGE;
}

enum class SourceTaskName : TaskName {
  TAGS, DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL;

  override val group: TaskGroup = TaskGroup.SOURCE
}

interface TaskName {
  val group: TaskGroup
  fun taskName(library: XtrasLibrary, target: KonanTarget? = null) =
    "xtras${group.name.lowercase().capitalized()}${
      toString().lowercase().capitalized()
    }${library.name.capitalized()}${target?.kotlinTargetName ?: ""}"
}

enum class PackageTaskName : TaskName {
  CREATE, EXTRACT, DOWNLOAD, PROVIDE;

  override val group: TaskGroup = TaskGroup.PACKAGE
}

fun XtrasLibrary.registerTasks() {
  project.logDebug("$name::registerTasks()")
  when (sourceConfig) {
    is GitSourceConfig -> registerGitSourceTasks()
  }
}

fun ExecSpec.xtrasCommandLine(vararg args: Any) {
  commandLine(args.map { if (it is File) it.mixedPath else it })
}

private val hostIsMingw = HostManager.hostIsMingw

val File.mixedPath: String
  get() = absolutePath.let {
    if (hostIsMingw) it.replace("\\", "/") else it
  }