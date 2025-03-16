package org.danbrough.xtras

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.invoke
import javax.inject.Inject

@Suppress("MemberVisibilityCanBePrivate")
open class Xtras @Inject constructor(project: Project) {

  val description: Property<String?> =
    project.xtrasProperty<String?>("$XTRAS_EXTN_NAME.description", null)

  val logger: Logger by lazy {
    XtrasLoggerImpl(
      project,
      project.xtrasPropertyValue("$XTRAS_EXTN_NAME.log.tag") { "XTRAS" },
      logToStdout = project.xtrasPropertyValue("$XTRAS_EXTN_NAME.log.stdout") { true },
      logToGradle = project.xtrasPropertyValue("$XTRAS_EXTN_NAME.log.gradle") { false }
    )
  }

  fun logging(action: Action<Logger>) {
    action.invoke(logger)
  }

  val android = XtrasAndroid(project)

  fun android(action: Action<XtrasAndroid>) {
    action.invoke(android)
  }

  val binaries = XtrasBinaries(project)

  fun binaries(action: Action<XtrasBinaries>) = action.invoke(binaries)
}