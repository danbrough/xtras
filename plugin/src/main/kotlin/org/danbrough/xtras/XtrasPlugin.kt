package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

const val XTRAS_PLUGIN_ID = XTRAS_PACKAGE
const val XTRAS_EXTENSION_NAME = "xtras"

class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    target.run {
      logInfo("XtrasPlugin.apply() project:${target.path}")

      val xtras = extensions.create(XTRAS_EXTENSION_NAME, XtrasExtension::class.java)
      xtras.nativeTargets.convention(emptyList())


      afterEvaluate {


        val kotlin = target.extensions.findByName("kotlin")
        if (kotlin is KotlinMultiplatformExtension) {
          xtras.nativeTargets.convention(
            kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget })
        }

        registerMiscTasks(xtras)
      }
    }

}

internal fun Project.registerMiscTasks(xtras:XtrasExtension) {

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