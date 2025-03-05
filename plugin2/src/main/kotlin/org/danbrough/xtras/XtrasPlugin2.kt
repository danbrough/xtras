package org.danbrough.xtras

import org.danbrough.xtras.XtrasProperty.Companion.xtrasProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate

class XtrasPlugin2 : Plugin<Project> {
  override fun apply(target: Project) {
    println("APPLYING XtrasPlugin2")

    val s by target.xtrasProperty<String>("xtras2.message", "default message")

    println("xtras2.message is $s")

  }
}