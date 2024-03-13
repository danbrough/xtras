package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

const val XTRAS_PLUGIN_ID = XTRAS_PACKAGE
const val XTRAS_EXTENSION_NAME = "xtras"

class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    target.run {
      logInfo("XtrasPlugin.apply() project:${target.path}")

      val xtras = extensions.create(XTRAS_EXTENSION_NAME, XtrasExtension::class.java)
      xtras.nativeTargets.convention(emptyList())


      afterEvaluate {
        registerMiscTasks()

        val kotlin = target.extensions.findByName("kotlin")
        if (kotlin is KotlinMultiplatformExtension) {
          xtras.nativeTargets.convention(
            kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget })
        }
      }
    }

}

internal fun Project.registerMiscTasks() {

  val kotlin = extensions.findByName("kotlin")
  if (kotlin is KotlinMultiplatformExtension) {
    tasks.register("xtrasTargets") {
      doFirst {
        kotlin.targets.all {
          logInfo("target: $this")
        }
      }
    }
  }
}