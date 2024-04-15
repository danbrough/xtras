package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskConfig
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.logDebug
import org.gradle.api.tasks.Exec
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
    }${library.name.capitalized()}${target?.kotlinTargetName ?: ""}"
}

enum class SourceTaskName : TaskName {
  TAGS, DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL;

  override val group: TaskGroup = TaskGroup.SOURCE
}

enum class InteropsTaskName : TaskName {
  GENERATE;

  override val group: TaskGroup = TaskGroup.CINTEROPS
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
  if (cinteropsConfig != null)
    registerCInteropsTasks()

  fun TaskConfig.run() = xtras.nativeTargets.get().forEach {
    invoke(it)
  }

  taskPrepareSource?.run()

  taskConfigureSource?.run()

  taskCompileSource?.run()

  taskInstallSource?.run()
}

@XtrasDSL
fun XtrasLibrary.prepareSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskPrepareSource = sourceTask(SourceTaskName.PREPARE, dependsOn) {
    block(it)
  }
}


@XtrasDSL
fun XtrasLibrary.configureSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskConfigureSource = sourceTask(SourceTaskName.CONFIGURE, dependsOn) {
    block(it)
  }
}


@XtrasDSL
fun XtrasLibrary.compileSource(
  dependsOn: SourceTaskName? = SourceTaskName.CONFIGURE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskCompileSource = sourceTask(SourceTaskName.COMPILE, dependsOn) { target ->
    block(target)
  }
}


@XtrasDSL
fun XtrasLibrary.installSource(
  dependsOn: SourceTaskName? = SourceTaskName.COMPILE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskInstallSource = sourceTask(SourceTaskName.INSTALL, dependsOn) {
    block(it)
  }
}
