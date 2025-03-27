package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.konan.target.HostManager

class XtrasBinaries(project: Project) {

  companion object {
    private const val PROPERTY_XTRAS_BIN = "$XTRAS_EXTN_NAME.bin"

    private fun Project.xtrasBinProperty(name: String) =
      project.xtrasProperty("$PROPERTY_XTRAS_BIN.$name") {
        if (HostManager.hostIsLinux) "/usr/bin/$name" else name
      }
  }

  val bash: Property<String> = project.xtrasBinProperty("bash")

  val git: Property<String> = project.xtrasBinProperty("git")
}
