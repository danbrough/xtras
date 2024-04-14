package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import kotlin.reflect.KProperty

internal class ToolsDelegate(val project: Project) {
  private val toolMap = mutableMapOf<String, String>()

  operator fun getValue(tools: Tools, property: KProperty<*>): String {
    val name = property.name
    return toolMap.getOrPut(name) {
      project.projectProperty<String>("xtras.tools.$name") { name }
    }
  }

  operator fun setValue(tools: Tools, property: KProperty<*>, value: String) {
    project.logError("setting ${property.name} to $value")
    toolMap[property.name] = value
  }
}

class Tools internal constructor(props: ToolsDelegate) {
  /**
   * Can be set by the project property "xtras.tools.git".
   * Defaults to: "git"
   */
  var git: String by props

  var autoreconf: String by props

  var make: String by props
}