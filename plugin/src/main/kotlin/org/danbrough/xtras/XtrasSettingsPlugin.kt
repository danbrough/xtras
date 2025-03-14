package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class XtrasSettingsPlugin : Plugin<Settings>{
  override fun apply(target: Settings) {
    val xtrasDir = target.xtrasDir
    println("$PROPERTY_XTRAS_DIR is: $xtrasDir")

    target.dependencyResolutionManagement {

    }
  }
}


