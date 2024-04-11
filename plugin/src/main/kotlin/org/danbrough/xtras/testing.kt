package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest


fun Project.xtrasTesting(block: AbstractTestTask.() -> Unit = {}) =
  tasks.withType<AbstractTestTask> {
    if (this is Test) {
      useJUnitPlatform()
    }
    testLogging {
      events = setOf(
        TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
      )
      exceptionFormat = TestExceptionFormat.FULL
      showStandardStreams = true
      showStackTraces = true
    }

    outputs.upToDateWhen {
      false
    }
    block()
  }


fun Project.xtrasEnableTestExes(
  configPrefix: String,
  `package`: String = "${group}.tests",
  buildTypes: List<NativeBuildType> = listOf(NativeBuildType.DEBUG),
  tests: List<String>
) {
  val kotlin = kotlinExtension as KotlinMultiplatformExtension

  /**
   * Create and configure native executable binaries for each test.
   */
  kotlin.targets.withType<KotlinNativeTarget> {
    binaries {
      tests.forEach { testName ->
        executable(testName, buildTypes) {
          entryPoint = "$`package`.main${testName.capitalized()}"
          compilation = compilations.getByName("test")
          runTask?.apply {
            //kotlinx.io uses $TMP for the temporary directory location
            args(if (extra.has("args")) extra["args"].toString().split(",") else emptyList())
            if (!environment.contains("TMP"))
              environment("TMP", System.getProperty("java.io.tmpdir"))
            project.properties.forEach { (key, value) ->
              if (key.startsWith("$configPrefix.")) {
                val envKey = key.replace('.', '_').uppercase()
                environment(envKey, value!!)
              }
            }
          }
        }
      }
    }
  }

  /**
   * Configure JVM tests as well
   */
  tasks.withType<KotlinJvmTest> {
    if (!environment.contains("TMP"))
      environment("TMP", System.getProperty("java.io.tmpdir"))

    systemProperty("args",if (extra.has("args")) extra["args"].toString() else "")

    //if (extra.has("args"))
    project.properties.forEach { (key, value) ->
      if (key.startsWith("$configPrefix.")) {
        val envKey = key.replace('.', '_').uppercase()
        environment(envKey, value!!)
      }
    }
  }
}
