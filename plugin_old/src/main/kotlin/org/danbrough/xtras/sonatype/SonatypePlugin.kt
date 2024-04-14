package org.danbrough.xtras.sonatype

import org.danbrough.xtras.logInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

class SonatypePlugin : Plugin<Project> {
  override fun apply(target: Project):Unit  = target.run {
    logInfo("SonatypePlugin.apply()")
    configurePublishing()
  }
}