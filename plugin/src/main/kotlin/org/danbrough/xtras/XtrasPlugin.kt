package org.danbrough.xtras

import org.danbrough.xtras.Xtras.Companion.createXtrasExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.createXtrasExtension()
  }
}