package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager


class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    target.run {
      if (parent != null) error("Xtras plugin must be applied on the parent project")
      logInfo("XtrasPlugin.apply() project:${target.path} parent: ${parent?.name}")

      val xtras = extensions.create(XTRAS_EXTENSION_NAME, Xtras::class.java).apply {
        nativeTargets.convention(emptyList())
        libraries.convention(emptyList())

        ldLibraryPath.convention(libraries.map { libs ->
          pathOf(libs.map { it.libsDir(HostManager.host).resolve("lib") })
        })
        //ldLibraryPath.convention( )
      }

      configureExtras(xtras)

      afterEvaluate {

        val kotlin = target.extensions.findByName("kotlin")
        if (kotlin is KotlinMultiplatformExtension) {
          xtras.nativeTargets.convention(
            kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget })
        }

        registerMiscTasks()
      }
    }
}

internal fun Project.registerMiscTasks() {

  val kotlin = extensions.findByName("kotlin")

  if (kotlin is KotlinMultiplatformExtension) {
    tasks.register("xtrasTargets") {
      group = XTRAS_TASK_GROUP
      description = "Lists all of the active kotlin targets"

      doFirst {
        kotlin.targets.all {
          logInfo("${project.group}.${project.name} -> target: $targetName")
        }
      }
    }
  }


  tasks.withType<Exec> {
    environment(
      HostManager.host.envLibraryPathName,
      pathOf(xtras.ldLibraryPath.get(), environment[HostManager.host.envLibraryPathName])
    )
  }

}

private fun Project.configureExtras(xtras: Xtras) {
  logTrace("configureExtras(): $name")

  findProperty(Xtras.PROJECT_GROUP)?.also {
    group = it.toString()
  } ?: logInfo("${Xtras.PROJECT_GROUP} not specified")

  findProperty(Xtras.PROJECT_VERSION)?.also {
    version = it.toString()
  } ?: logDebug("${Xtras.PROJECT_VERSION} not specified")

  xtrasPublishing { }

}