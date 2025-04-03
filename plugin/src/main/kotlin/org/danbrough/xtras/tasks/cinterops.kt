package org.danbrough.xtras.tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

class CInteropsConfig(project: Project) {
  val packageName: Property<String> =
    project.objects.property<String>().convention(project.provider {
      "${project.group}.${project.name}.cinterops"
    })
}