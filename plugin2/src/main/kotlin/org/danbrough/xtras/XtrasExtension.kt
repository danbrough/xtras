package org.danbrough.xtras

import org.danbrough.xtras.XtrasProperty.Companion.xtrasProperty
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import javax.inject.Inject

open class XtrasExtension @Inject constructor(
  private val project: Project,
  objects: ObjectFactory
) {
  val description: Property<String?> =
    project.xtrasProperty<String?>("xtras2.message",null).toProperty()

  init {
    println("CREATE XTRAS EXTN: ${project.path}")
  }

  companion object {
    internal fun Project.createXtrasExtension() = extensions.create<XtrasExtension>("xtras2")
  }
}