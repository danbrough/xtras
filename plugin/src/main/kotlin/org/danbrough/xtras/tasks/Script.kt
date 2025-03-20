package org.danbrough.xtras.tasks

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.XtrasLibrary
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

abstract class Script(val library: XtrasLibrary) {
  val environment: ScriptEnvironment = mutableMapOf()
}

var environmentDefault: Script.(target: KonanTarget) -> Unit = {
  if (HostManager.hostIsLinux) {
    environment["PATH"] = "/bin:/usr/bin:/usr/local/bin"
  }
}