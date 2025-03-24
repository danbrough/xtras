package org.danbrough.xtras

import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager

class XtrasEnvironment(val project: Project) {
  companion object {
    private const val XTRAS_ENV = "$XTRAS_EXTN_NAME.env"
  }

  val pathDefault = project.xtrasProperty<String>("$XTRAS_ENV.bin") {
    if (HostManager.hostIsLinux) "/bin:/usr/bin:/usr/local/bin"
    else if (HostManager.hostIsMac) "/bin:/usr/bin:/usr/local/bin"
    else TODO("Need to set a XtrasEnvironment.pathDefault for ${HostManager.host}")
  }
}