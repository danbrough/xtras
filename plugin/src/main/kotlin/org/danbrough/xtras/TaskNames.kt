@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.KonanTarget

object TaskNames {
  const val TASK_PREFIX = XTRAS_EXTN_NAME
  const val ACTION_DOWNLOAD = "Download"
  const val GROUP_SOURCE = "Source"


  fun create(group: String, action: String, target: KonanTarget? = null): String {
    buildString {
      append(TASK_PREFIX)
      append(action)

    }
    TODO()
  }
}