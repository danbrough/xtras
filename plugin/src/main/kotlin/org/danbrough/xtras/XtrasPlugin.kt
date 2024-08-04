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
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.konan.target.HostManager


class XtrasPlugin : Plugin<Any> {
  override fun apply(target: Any) {
    if (target !is Project) return
    target.run {
      if (parent != null) error("Xtras plugin should be applied to the root project only")

      //logInfo("XtrasPlugin.apply() project:${target.path} parent: ${parent?.name}")

      allprojects {
        logTrace("${this.name}::configuring xtras")
        apply<MavenPublishPlugin>()
        apply<SigningPlugin>()

        val xtras = xtrasExtension.apply {
          nativeTargets.convention(emptyList())
          libraries.convention(emptyList())

          repoIDFileName.convention(project.provider {
            "sonatypeRepoID_${rootProject.name}_${rootProject.group}"
          })
          //by default share a single repoID for entire project

          repoIDFile.convention(repoIDFileName.map {
            rootProject.layout.buildDirectory.file(it).get()
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

        configureExtras(xtras)
      }
    }
  }
}

val Project.xtrasExtension: Xtras
  get() = extensions.findByType<Xtras>() ?: extensions.create(
    XTRAS_EXTENSION_NAME,
    Xtras::class.java
  )

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


/*  tasks.withType<Exec> {
    environment(
      HostManager.host.envLibraryPathName,
      pathOf(
        xtrasExtension.ldLibraryPath(),
        environment[HostManager.host.envLibraryPathName]
      )
    )
  }*/

}

private fun Project.configureExtras(xtras: Xtras) {
  //logDebug("configureExtras(): $name")

  findProperty(Xtras.Constants.Properties.PROJECT_GROUP)?.also {
    group = it.toString()
  } ?: logTrace("${Xtras.Constants.Properties.PROJECT_GROUP} not specified. Defaulting to $group")

  findProperty(Xtras.Constants.Properties.PROJECT_VERSION)?.also {
    version = it.toString()
  }
    ?: logTrace("${Xtras.Constants.Properties.PROJECT_VERSION} not specified. Defaulting to $version")

  logDebug("name:$name $group:$version")

  xtrasPublishing()

  registerKonanDepsTasks()

  configureProjectTasks(xtras)
}

/**
 * Setup environment for executable and test tasks
 */
private fun Project.configureProjectTasks(xtras: Xtras) {

  afterEvaluate {
    val exes = kotlinBinaries { it is Executable }
    logTrace("configureProjectTasks() libraries: ${xtras.libraries.get()} exeCount:${exes.size}")

    exes.forEach { exe ->
      val runTask = (exe as Executable).runTask!!
      val ldPath = exe.xtrasLibraryPath()
      logDebug("configureProjectTasks:$exe ldPath: $ldPath")
      runTask.environment[HostManager.host.envLibraryPathName] = ldPath
    }
  }
}