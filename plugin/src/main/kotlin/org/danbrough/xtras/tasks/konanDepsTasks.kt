package org.danbrough.xtras.tasks


import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.xtrasName
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import java.io.File


private val KonanTarget.konanDepsTaskName: String
  get() = "xtrasKonanDeps${xtrasName.capitalized()}"

fun Task.xtrasKonanDeps(target: KonanTarget) {
  val depsTaskName = target.konanDepsTaskName
  if (project.rootProject.tasks.findByName(depsTaskName) == null)
    project.registerKonanDepsTask(target)

  dependsOn(":$depsTaskName")
}

internal fun Project.registerKonanDepsTasks() {
  afterEvaluate {

    tasks.withType<CInteropProcess> {
      xtrasKonanDeps(konanTarget)
    }

    tasks.withType<KotlinNativeCompile> {
      @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
      xtrasKonanDeps(KonanTarget.predefinedTargets[target]!!)
    }
  }
}

private fun Project.registerKonanDepsTask(target: KonanTarget) {

  val generateDepsProjectTaskName =
    "xtrasGenerateKonanDepsProject${target.xtrasName.capitalized()}"
  //xError("registerKonanDepsTask: $generateDepsProjectTaskName")
//  xInfo("$name:registerKonanDepsTask() $generateDepsProjectTaskName")

  if (rootProject.tasks.findByName(generateDepsProjectTaskName) != null) return

  val depsProjectDir =
    File(System.getProperty("java.io.tmpdir"), "konanDeps${target.xtrasName.capitalized()}")

  rootProject.tasks.register(generateDepsProjectTaskName) {
    outputs.dir(depsProjectDir)
    //xError("registered task: $name")
    doFirst {
      depsProjectDir.mkdirs()
      depsProjectDir.resolve("gradle.properties").writeText(
        """
        kotlin.native.ignoreDisabledTargets=true
        org.gradle.parallel=false
        org.gradle.caching=false

      """.trimIndent()
      )


      depsProjectDir.resolve("settings.gradle.kts").also {
        if (!it.exists()) it.createNewFile()
      }

      depsProjectDir.resolve("build.gradle.kts").printWriter().use { output ->
        output.println(
          """
          plugins {
            kotlin("multiplatform") version "${this@registerKonanDepsTask.kotlinExtension.coreLibrariesVersion}"
          }

          repositories {
            mavenCentral()
          }

          kotlin {
          	${target.presetName}()
          }
      """.trimIndent()
        )
      }


      depsProjectDir.resolve("src/commonMain/kotlin").apply {
        mkdirs()
        resolve("test.kt").writeText(
          """
              fun test(){
                println("some code to compile")
              }
           """.trimIndent()
        )
      }
    }
  }


  rootProject.tasks.register(
    target.konanDepsTaskName, GradleBuild::class.java
  ) {

    dependsOn(generateDepsProjectTaskName)
    group = TaskNames.XTRAS_TASK_GROUP
    description = "Placeholder task for pre-downloading konan $target dependencies"
    dir = depsProjectDir
    val taskName = "build"
    tasks = listOf(taskName)
  }
}

