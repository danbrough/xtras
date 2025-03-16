@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras


import org.danbrough.xtras.Tasks.create
import org.jetbrains.kotlin.konan.target.KonanTarget

object Tasks {
  const val XTRAS_TASK_GROUP = XTRAS_EXTN_NAME
  const val TASK_PREFIX = XTRAS_EXTN_NAME
  const val ACTION_DOWNLOAD = "download"
  const val ACTION_EXTRACT = "extract"
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

internal fun XtrasLibrary.xtrasSourceDownloadTaskName() =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_DOWNLOAD, name)

internal fun XtrasLibrary.xtrasSourceExtractTaskName(konanTarget: KonanTarget) =
  create(Tasks.GROUP_SOURCE, Tasks.ACTION_EXTRACT, name, konanTarget)