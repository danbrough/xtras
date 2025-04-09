@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras


import org.danbrough.xtras.TaskNames.create
import org.jetbrains.kotlin.konan.target.KonanTarget

object TaskNames {
  const val XTRAS_TASK_GROUP = XTRAS_EXTN_NAME
  const val TASK_PREFIX = XTRAS_EXTN_NAME

  const val ACTION_DOWNLOAD = "download"
  const val ACTION_EXTRACT = "extract"

  //const val ACTION_PREPARE = "prepare"
  //const val ACTION_CONFIGURE = "configure"
  const val GROUP_BUILD = "build"
  const val GROUP_PACKAGE = "package"

  const val GROUP_SOURCE = "source"


  fun create(
    group: String, action: String?, libraryName: String? = null, target: KonanTarget? = null
  ): String = buildString {
    append(TASK_PREFIX)
    if (libraryName != null) append(libraryName.replaceFirstChar { it.uppercase() })
    append(group.replaceFirstChar { it.uppercase() })
    if (action != null)
      append(action.replaceFirstChar { it.uppercase() })
    if (target != null) append(target.xtrasName.replaceFirstChar { it.uppercase() })
  }
}

internal fun XtrasLibrary.taskNameSourceDownload() =
  create(TaskNames.GROUP_SOURCE, TaskNames.ACTION_DOWNLOAD, name)

fun XtrasLibrary.taskNameSourceExtract(konanTarget: KonanTarget) =
  create(TaskNames.GROUP_SOURCE, TaskNames.ACTION_EXTRACT, name, konanTarget)

internal fun XtrasLibrary.taskNameBuild(konanTarget: KonanTarget) =
  create(TaskNames.GROUP_BUILD, null, name, konanTarget)

internal fun XtrasLibrary.taskNamePackage(konanTarget: KonanTarget) =
  create(TaskNames.GROUP_PACKAGE, null, name, konanTarget)

internal fun XtrasLibrary.taskNamePackageExtract(konanTarget: KonanTarget) =
  create(TaskNames.GROUP_PACKAGE, TaskNames.ACTION_EXTRACT, name, konanTarget)