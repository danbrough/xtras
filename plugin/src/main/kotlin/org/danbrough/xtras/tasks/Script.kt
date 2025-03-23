package org.danbrough.xtras.tasks

import org.danbrough.xtras.ScriptEnvironment
import org.danbrough.xtras.XtrasLibrary
import org.gradle.api.Action
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

class Script(
  val library: XtrasLibrary, val task: ScriptTask, val target: KonanTarget
) {
  val environment: ScriptEnvironment = mutableMapOf()

  init {
    environmentDefault()
  }

  internal var action: Action<ScriptTask>? = null

  fun script(action: Action<ScriptTask>) {
    this.action = action
  }
}

var environmentDefault: Script.() -> Unit = {
  if (HostManager.hostIsLinux) {
    environment["PATH"] = "/bin:/usr/bin:/usr/local/bin"
    environment["MESSAGE"] = "Hello From Script.kt"
  }
}