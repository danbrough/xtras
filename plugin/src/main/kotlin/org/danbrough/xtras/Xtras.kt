package org.danbrough.xtras

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("MemberVisibilityCanBePrivate")
open class Xtras @Inject constructor(project: Project) {

  companion object {
    internal fun Project.createXtrasExtension() = extensions.create<Xtras>("xtras")
  }

  init {
    println("CREATING XTRAS EXTENSION FOR: ${project.path}")
  }

  val description: Property<String?> =
    project.xtrasProperty<String?>("xtras.description", null)

  val message: Property<String?> = project.objects.property<String?>()
  val logger = XtrasLogger(project)

  fun logging(action: Action<XtrasLogger>) {
    action.invoke(logger)
  }

  val android = XtrasAndroid(project)

  fun android(action: Action<XtrasAndroid>) {
    action.invoke(android)
  }
}