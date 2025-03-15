@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.KonanTarget

object Tasks {
  const val XTRAS_TASK_GROUP = XTRAS_EXTN_NAME
  const val TASK_PREFIX = XTRAS_EXTN_NAME
  const val ACTION_DOWNLOAD = "Download"
  const val GROUP_SOURCE = "Source"

  fun create(group: String, action: String, target: KonanTarget? = null): String = buildString {
    append(TASK_PREFIX)
    append(group)
    append(action)
    if (target != null) append(target.xtrasName)
  }

  val TASK_SOURCE_DOWNLOAD = create(GROUP_SOURCE, ACTION_DOWNLOAD)

}