package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibraryExtension
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.logDebug
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget


enum class TaskGroup {
  SOURCE, PACKAGE;
}

enum class SourceTaskName : TaskName {
  TAGS, DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL;

  override val group: TaskGroup = TaskGroup.SOURCE
}

interface TaskName {
  val group: TaskGroup
  fun taskName(library: XtrasLibraryExtension? = null, target: KonanTarget? = null) =
    "xtras${group.name.lowercase().capitalized()}${
      toString().lowercase().capitalized()
    }${library?.name?.capitalized() ?: ""}${target?.kotlinTargetName ?: ""}"
}

enum class PackageTaskName : TaskName {
  CREATE, EXTRACT, DOWNLOAD, PROVIDE;

  override val group: TaskGroup = TaskGroup.PACKAGE
}

fun XtrasLibraryExtension.registerTasks() {
  project.logDebug("$name::registerTasks()")
  when (sourceConfig) {
    is GitSourceConfig -> registerGitSourceTasks()
  }
}

fun Exec.xtrasCommandLine(vararg args: Any) {
  commandLine(*args)
}