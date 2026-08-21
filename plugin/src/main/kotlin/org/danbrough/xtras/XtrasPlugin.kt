package org.danbrough.xtras

import org.danbrough.xtras.tasks.registerKonanDepsTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


val Project.xtrasExtension: Xtras
  get() = extensions.findByType<Xtras>() ?: extensions.create(
    XTRAS_EXTN_NAME,
    Xtras::class.java
  )

class XtrasPlugin : Plugin<Project> {
  override fun apply(target: Project) {


    target.run {
      xInfo("XtrasPlugin::apply project:$name path:$path")
      if (parent != null) error("Xtras plugin should be applied to the root project only. Not $name")
      logger.info("XtrasPlugin APPLY: $path")


      afterEvaluate {
        allprojects {
          xtrasExtension.also { xtras ->
            xError("KONAN DIR: $xtrasKonanDir")
            xInfo("$name: created xtras extension: $xtras")
            //xWarn("not registering konan deps tasks")


            //configureExtras(xtras)
            //registerMiscTasks(xtras)
            configureExtras(xtras)
            registerKonanDepsTasks()
          }
        }
      }
    }

    /*
          if (parent != null) error("Xtras plugin should be applied to the root project only")
      logError("XtrasPlugin APPLY: $path")

      //logInfo("XtrasPlugin.apply() project:${target.path} parent: ${parent?.name}")

      allprojects {
        logWarn("XtrasPlugin::apply: path:$path")
        val xtras = xtrasExtension

        configureExtras(xtras)
        registerMiscTasks(xtras)
      }
     */
  }
}

private fun Project.configureExtras(xtras: Xtras) {
  xDebug("configureExtras(): $path")

  xtras.nativeTargets.convention(providers.provider {
    val kotlin = extensions.findByName("kotlin")
    if (kotlin is KotlinMultiplatformExtension) {
      kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget }
    } else emptyList()
  })

  findProperty(Constants.Properties.PROJECT_GROUP)?.also {
    group = it.toString()
  } ?: xError("Failed to find project.group in $name defaulting to $group")

  findProperty(Constants.Properties.PROJECT_VERSION)?.also {
    version = it.toString()
  } ?: xError("Failed to find project.version in $name defaulting to $version")
}

/*
private fun Project.configureExtras(xtras: Xtras) {
  logDebug("configureExtras(): $path")

  xtras.nativeTargets.convention(providers.provider {
    val kotlin = extensions.findByName("kotlin")
    if (kotlin is KotlinMultiplatformExtension) {
      kotlin.targets.withType<KotlinNativeTarget>().map { it.konanTarget }
    } else emptyList()
  })

  /*  xtras.repoIDFileName.convention(project.provider {
      "sonatypeRepoID_${rootProject.name}_${rootProject.group}"
    })*/
  //by default share a single repoID for entire project


  (System.getenv("ANDROID_NDK") ?: System.getenv("ANDROID_NDK_ROOT"))?.also {
    xtras.androidConfig.ndkDir = File(it)
  }

  findProperty(Xtras.Constants.Properties.PROJECT_GROUP)?.also {
    group = it.toString()
  }
    ?: logTrace("${Xtras.Constants.Properties.PROJECT_GROUP} not specified. Defaulting to $group")

  findProperty(Xtras.Constants.Properties.PROJECT_VERSION)?.also {
    version = it.toString()
  }
    ?: logTrace("${Xtras.Constants.Properties.PROJECT_VERSION} not specified. Defaulting to $version")

  if (xtrasProperty(Xtras.Constants.Properties.XTRAS_PUBLISHING) { false })
    xtrasPublishing()

  registerKonanDepsTasks()

  configureProjectTasks(xtras)
}
 */