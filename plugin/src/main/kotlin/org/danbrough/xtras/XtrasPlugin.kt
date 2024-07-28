package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager


class XtrasPlugin : Plugin<Any> {
  override fun apply(target: Any) {
    if (target !is Project) return
    target.run {
      if (parent != null) error("Xtras plugin should be applied to the root project only")

      logInfo("XtrasPlugin.apply() project:${target.path} parent: ${parent?.name}")

      allprojects {
        logWarn("${this.name}::applying maven publish plugin")
        apply<MavenPublishPlugin>()
        apply<SigningPlugin>()
        xtrasExtension
        configureExtras()
      }
    }
  }
}

val Project.xtrasExtension: Xtras
  get() = extensions.findByType<Xtras>() ?: extensions.create(
    XTRAS_EXTENSION_NAME,
    Xtras::class.java
  ).apply {
    nativeTargets.convention(emptyList())
    libraries.convention(emptyList())

    repoIDFileName.convention(project.provider {
      "sonatypeRepoID_${rootProject.name}_${rootProject.group}"
    })
    //by default share a single repoID for entire project

    repoIDFile.convention(repoIDFileName.map { rootProject.layout.buildDirectory.file(it).get() })


    ldLibraryPath.convention(libraries.map { libs ->
      pathOf(libs.map { it.libsDir(HostManager.host).resolve("lib") })
    })

    afterEvaluate {

      val kotlin = extensions.findByName("kotlin")
      if (kotlin is KotlinMultiplatformExtension) {
        nativeTargets.convention(
          kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget })
      }

      registerMiscTasks()
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
      pathOf(
        xtrasExtension.ldLibraryPath.get(),
        environment[HostManager.host.envLibraryPathName]
      )
    )
  }

}

private fun Project.configureExtras() {
  logDebug("configureExtras(): $name")

  findProperty(Xtras.Constants.Properties.PROJECT_GROUP)?.also {
    group = it.toString()
  } ?: logTrace("${Xtras.Constants.Properties.PROJECT_GROUP} not specified. Defaulting to $group")

  findProperty(Xtras.Constants.Properties.PROJECT_VERSION)?.also {
    version = it.toString()
  }
    ?: logTrace("${Xtras.Constants.Properties.PROJECT_VERSION} not specified. Defaulting to $version")

  logTrace("name:$name group: $group version: $version")

  xtrasPublishing()


    registerKonanDepsTasks()

}