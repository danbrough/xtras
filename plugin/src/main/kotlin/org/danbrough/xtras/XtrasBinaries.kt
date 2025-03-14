package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class XtrasBinaries(project: Project) {
  companion object {
    const val PROPERTY_XTRAS_BIN = "$XTRAS_PROPERTY.bin"
  }

  val git: Property<String> = project.xtrasProperty("$PROPERTY_XTRAS_BIN.git") {

    ""
  }

}