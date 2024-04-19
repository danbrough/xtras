@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.resolveBinariesFromMaven
import org.danbrough.xtras.xtrasCommandLine
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

internal fun XtrasLibrary.registerPackageTasks(target: KonanTarget) {
  registerPackageCreateTask(target)
  registerPackageProvideTask(target)
  registerPackageExtractTask(target)
}

private fun XtrasLibrary.registerPackageCreateTask(target: KonanTarget) {
  if (taskInstallSource == null) return

  project.tasks.register<Exec>(
    PackageTaskName.CREATE.taskName(
      this@registerPackageCreateTask,
      target
    )
  ) {
    group = XTRAS_TASK_GROUP
    dependsOn(SourceTaskName.INSTALL.taskName(this@registerPackageCreateTask, target))
    val packageFile = packageFile(target)
    onlyIf { !packageFile.exists() }
    inputs.dir(buildDir(target))
    workingDir(buildDir(target))
    outputs.file(packageFile)
    xtrasCommandLine(
      "tar",
      "cvpfz",
      packageFile,
      "--exclude=**share",
      "--exclude=**pkgconfig",
      "./"
    )
    doLast {
      sourceDir(target).deleteRecursively()
      buildDir(target).deleteRecursively()
    }
  }
}

private fun XtrasLibrary.registerPackageProvideTask(target: KonanTarget) {
  val packageFile = packageFile(target)
  val taskName = PackageTaskName.PROVIDE.taskName(this@registerPackageProvideTask, target)
  project.tasks.register(taskName) {
    group = XTRAS_TASK_GROUP
    onlyIf { !packageFile.exists() }
    if (buildEnabled && !packageFile.exists())
      dependsOn(PackageTaskName.CREATE.taskName(this@registerPackageProvideTask, target))
    actions.add {
      if (!packageFile.exists()) {
        resolveBinariesFromMaven(target)?.copyTo(packageFile(target))
      }
    }

    outputs.file(packageFile)
  }
}

private fun XtrasLibrary.registerPackageExtractTask(target: KonanTarget) {
  if (taskInstallSource == null) return
  val libsDir = libsDir(target)
  val packageFile = packageFile(target)
  val taskName = PackageTaskName.EXTRACT.taskName(this@registerPackageExtractTask, target)
  project.tasks.register<Exec>(taskName) {
    group = XTRAS_TASK_GROUP
    doFirst {
      if (libsDir.exists()) libsDir.deleteRecursively()
      libsDir.mkdirs()
    }
    dependsOn(PackageTaskName.PROVIDE.taskName(this@registerPackageExtractTask, target))
    inputs.file(packageFile)
    outputs.dir(libsDir)
    workingDir(libsDir)
    xtrasCommandLine("tar", "xpfz", packageFile)
  }
}


