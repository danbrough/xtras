package org.danbrough.xtras.tasks

import com.android.build.gradle.internal.crash.afterEvaluate
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.envLibraryPathName
import org.danbrough.xtras.kotlinBinaries
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logError
import org.danbrough.xtras.logTrace
import org.danbrough.xtras.pathOf
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.konan.target.HostManager
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
  TAGS, DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL, BUILD;

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
        taskInstallSource?.invoke(target)
        */

    registerPackageTasks(target)
  }

}
//
//private fun Project.configureProjectTasks(library: XtrasLibrary) {
//  logError("$name::configureProjectTasks(): $library")
//  afterEvaluate {
//    kotlinBinaries().forEach {
//      logError("binaries: $it")
//    }
//
//    kotlinBinaries { it is Executable }.forEach { exe ->
//      exe as Executable
//      val runTask = exe.runTask!!
//      val ldPath = pathOf(
//        runTask.environment[HostManager.host.envLibraryPathName],
//        library.libsDir.invoke(exe.target.konanTarget).resolve("libs")
//      )
//      logError("${project.name}::configureProjectTasks() exe:${exe} ldPath: $ldPath")
//      runTask.environment(HostManager.host.envLibraryPathName, ldPath)
//    }
//  }
//}
//
//
//
