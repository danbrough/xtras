package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceConfigure
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xInfo
import org.gradle.kotlin.dsl.register


fun XtrasLibrary.buildScript(config: ScriptTask.() -> Unit) {
  project.afterEvaluate {

    project.xInfo(
      "${this@buildScript.name}:configureSource(): targets: ${
        buildTargets.get().joinToString()
      }"
    )

    buildTargets.get().forEach { target ->
      project.tasks.register<ScriptTask>(taskNameSourceConfigure(target)) {
        dependsOn(this@buildScript.taskNameSourceExtract(target))
        this.target.set(target)
        workingDir = sourcesDirMap(target)
        description = "Configure the source for ${this@buildScript.name}"
        config()
      }
    }
  }
}

