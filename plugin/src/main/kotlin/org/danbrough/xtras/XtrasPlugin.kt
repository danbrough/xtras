package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

const val XTRAS_EXTENSION_NAME = "xtras"

class XtrasPlugin : Plugin<Project> {
	override fun apply(target: Project) =
		target.run {
			logInfo("XtrasPlugin.apply() project:${target.path}")

			val xtras = extensions.create(XTRAS_EXTENSION_NAME, XtrasExtension::class.java).apply {
				nativeTargets.convention(emptyList())
				libraries.convention(emptyList())

				ldLibraryPath.convention(libraries.map { libs ->
					pathOf(libs.map { it.libsDir(HostManager.host).resolve("lib") })
				})
				//ldLibraryPath.convention( )
			}

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
			pathOf(xtras.ldLibraryPath.get(),environment[HostManager.host.envLibraryPathName])
		)
	}

}