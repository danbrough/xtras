package org.danbrough.xtras

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.konan.target.KonanTarget
import javax.inject.Inject


@DslMarker
annotation class XtrasDSL

@Suppress("MemberVisibilityCanBePrivate")
abstract class Xtras @Inject constructor(val project: Project) {

  val description: Property<String> =
    project.xtrasProperty<String>("$XTRAS_EXTN_NAME.description")


//  fun logging(action: Action<Logger>) {
//    action.invoke(logger)
//  }

  val android = XtrasAndroid(project)

  fun android(action: Action<XtrasAndroid>) {
    action.invoke(android)
  }

  val binaries = XtrasBinaries(project)

  fun binaries(action: Action<XtrasBinaries>) = action.invoke(binaries)

  val environment = XtrasEnvironment(project)

  fun environment(action: Action<XtrasEnvironment>) = action.invoke(environment)

  @XtrasDSL
  abstract val nativeTargets: ListProperty<KonanTarget>

  companion object {
    val Project.xtras: Xtras
      get() = extensions.getByType<Xtras>()
  }
}