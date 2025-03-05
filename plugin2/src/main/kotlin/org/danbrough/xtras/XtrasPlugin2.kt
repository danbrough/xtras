package org.danbrough.xtras

import org.danbrough.xtras.XtrasExtension.Companion.createXtrasExtension
import org.danbrough.xtras.XtrasProperty.Companion.xtrasProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate

class XtrasPlugin2 : Plugin<Project> {
  override fun apply(target: Project) {
    println("APPLYING XtrasPlugin2")
    target.createXtrasExtension()
  }
}