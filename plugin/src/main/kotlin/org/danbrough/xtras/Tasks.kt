@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras


import org.danbrough.xtras.Tasks.create
import org.jetbrains.kotlin.konan.target.KonanTarget

object Tasks {
  const val XTRAS_TASK_GROUP = XTRAS_EXTN_NAME
  const val TASK_PREFIX = XTRAS_EXTN_NAME

  const val ACTION_DOWNLOAD = "download"
  const val ACTION_EXTRACT = "extract"
  const val ACTION_PREPARE = "prepare"
  const val ACTION_CONFIGURE = "configure"
  const val ACTION_BUILD = "build"

  const val GROUP_SOURCE = "source"

  fun create(
    group: String, action: String, libraryName: String? = null, target: KonanTarget? = null
  ): String = buildString {
    append(TASK_PREFIX)
    if (libraryName != null) append(libraryName.replaceFirstChar { it.uppercase() })
    append(group.replaceFirstChar { it.uppercase() })
    append(action.replaceFirstChar { it.uppercase() })
    if (target != null) append(target.xtrasName.replaceFirstChar { it.uppercase() })
  }
}

internal fun XtrasLibrary.taskNameSourceDownload() =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_DOWNLOAD, name)

internal fun XtrasLibrary.taskNameSourceExtract(konanTarget: KonanTarget) =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_EXTRACT, name, konanTarget)

internal fun XtrasLibrary.taskNameSourcePrepare(konanTarget: KonanTarget) =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_PREPARE, name, konanTarget)

internal fun XtrasLibrary.taskNameSourceConfigure(konanTarget: KonanTarget) =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_CONFIGURE, name, konanTarget)

internal fun XtrasLibrary.taskNameSourceBuild(konanTarget: KonanTarget) =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_BUILD, name, konanTarget)
