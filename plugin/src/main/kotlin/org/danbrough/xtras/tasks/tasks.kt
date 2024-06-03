package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.logDebug
import org.jetbrains.kotlin.konan.target.KonanTarget


enum class TaskGroup {
  CINTEROPS, SOURCE, PACKAGE;

  override fun toString(): String = name
}

interface TaskName {
  val group: TaskGroup

  fun taskName(library: XtrasLibrary, target: KonanTarget? = null) =
    "xtras${group.toString().lowercase().capitalized()}${
      toString().lowercase().capitalized()
    }${library.name.capitalized()}${target?.kotlinTargetName?.capitalized() ?: ""}"
}

enum class SourceTaskName : TaskName {
  TAGS, DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL,BUILD;

  override val group: TaskGroup = TaskGroup.SOURCE
}

enum class InteropsTaskName : TaskName {
  GENERATE;

  override val group: TaskGroup = TaskGroup.CINTEROPS
}


enum class PackageTaskName : TaskName {
  CREATE, EXTRACT, RESOLVE, DOWNLOAD;

  override val group: TaskGroup = TaskGroup.PACKAGE
}

fun XtrasLibrary.registerTasks() {
  project.logDebug("$name::registerTasks()")

  when (sourceConfig) {
    is GitSourceConfig -> registerGitSourceTasks()
  }

  if (cinteropsConfig != null && buildEnabled)
    registerCInteropsTasks()

  xtras.nativeTargets.get().forEach { target ->

    registerBuildTask(target)
/*
    taskPrepareSource?.invoke(target)

    taskConfigureSource?.invoke(target)

    taskCompileSource?.invoke(target)

    taskInstallSource?.invoke(target)*/

    registerPackageTasks(target)

  }




}



