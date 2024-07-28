package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import java.io.File


val KonanTarget.konanDepsTaskName: String
  get() = "xtrasKonanDeps${presetName.capitalized()}"

internal fun Project.registerKonanDepsTasks() {

  val generateDepsProjectTaskName = "xtrasGenerateKonanDepsProject"

  val depsProjectDir =
    rootProject.layout.buildDirectory.dir("konandeps").get().asFile

//  val depsProjectDir = File(System.getProperty("java.io.tmpdir"),"konandeps")
  if (parent == null) {
    rootProject.tasks.register(generateDepsProjectTaskName) {
      outputs.dir(depsProjectDir)
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
            kotlin("multiplatform") version "${KotlinVersion.CURRENT}"
          }
   
          repositories {
            mavenCentral()
          }

          kotlin {
      """.trimIndent()
          )
          KonanTarget.predefinedTargets.forEach { key, target ->
            output.println("\t${target.presetName}()")
          }
          output.println("}")
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

    KonanTarget.predefinedTargets.values.forEach { target ->
      rootProject.tasks.register(
        target.konanDepsTaskName, GradleBuild::class.java
      ) {
        dependsOn(generateDepsProjectTaskName)
        group = XTRAS_TASK_GROUP
        description = "Placeholder task for pre-downloading konan $target dependencies"
        dir = depsProjectDir
        val taskName = "compileKotlin${target.presetName.capitalized()}"
        tasks = listOf(taskName)
      }
    }
  }

  afterEvaluate {

    tasks.withType<CInteropProcess> {
      dependsOn(":${konanTarget.konanDepsTaskName}")
    }

    tasks.withType<KotlinNativeCompile> {
      dependsOn(":${KonanTarget.predefinedTargets[target]!!.konanDepsTaskName}")
    }
  }
}

