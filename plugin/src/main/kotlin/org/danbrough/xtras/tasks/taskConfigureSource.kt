package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceConfigure
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xInfo
import org.gradle.kotlin.dsl.register


fun XtrasLibrary.configureSource(config: ScriptTask.() -> Unit) {
  project.afterEvaluate {
    project.xInfo(
      "${this@configureSource.name}:configureSource(): targets: ${
        buildTargets.get().joinToString()
      }"
    )

    buildTargets.get().forEach { target ->
      project.tasks.register<ScriptTask>(taskNameSourceConfigure(target)) {
        dependsOn(this@configureSource.taskNameSourceExtract(target))
        workingDir = sourcesDirMap(target)
        description = "Configure the source for ${this@configureSource.name}"
        config()
      }
    }
  }
}

