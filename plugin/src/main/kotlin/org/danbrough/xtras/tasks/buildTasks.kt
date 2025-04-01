package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameBuild
import org.danbrough.xtras.taskNamePackage
import org.danbrough.xtras.taskNamePackageExtract
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xtrasName
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register


fun XtrasLibrary.buildScript(config: ScriptTask.() -> Unit) {
  project.afterEvaluate {

    xInfo(
      "${this@buildScript.name}:buildScript(): targets: ${
        buildTargets.get().joinToString()
      }"
    )

    buildTargets.get().forEach { target ->
      val taskNameBuild = taskNameBuild(target)
      val taskNamePackage = taskNamePackage(target)
      val packageFile = packageFileMap(target)

      val scriptTaskProvider = tasks.register<ScriptTask>(taskNameBuild) {
        dependsOn(this@buildScript.taskNameSourceExtract(target))
        this.target.set(target)
        workingDir = sourcesDirMap(target)
        onlyIf { !packageFile.exists() }
        description = "Builds ${this@buildScript.name} for ${target.xtrasName}"
        config()
      }

      tasks.register<Exec>(taskNamePackage) {
        dependsOn(taskNameBuild)
        onlyIf { !packageFile.exists() }
        outputs.file(packageFile)
        doFirst {
          workingDir = scriptTaskProvider.get().outputDirectory.get()
          commandLine(
            "tar", "cvpfz", packageFile, "--exclude=**share", "--exclude=**pkgconfig", "./"
          )
        }
      }

      tasks.register<Exec>(taskNamePackageExtract(target)) {
        dependsOn(taskNamePackage)
        onlyIf { packageFile.exists() }
        val libDir = libDirMap(target)
        workingDir(libDir)
        outputs.dir(libDir)
        commandLine("tar", "xvpfz", packageFile)
      }
    }
  }
}

