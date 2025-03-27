package org.danbrough.xtras

import org.danbrough.xtras.tasks.registerKonanDepsTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create


class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.run {
      extensions.create<Xtras>(XTRAS_EXTN_NAME).also { xtras ->
        xInfo("$name: created xtras extension: $xtras")
        registerKonanDepsTasks()
      }
    }
  }
}