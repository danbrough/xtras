package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create<Xtras>(XTRAS_EXTN_NAME)
  }
}