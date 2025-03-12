package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class XtrasSettingsPlugin : Plugin<Settings>{
  override fun apply(target: Settings) {
    println("XtrasSettingsPlugin")
  }
}