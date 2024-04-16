@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
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
  }
}

private fun XtrasLibrary.registerPackageProvideTask(target: KonanTarget) {
  val packageFile = packageFile(target)
  project.tasks.register(
    PackageTaskName.PROVIDE.taskName(
      this@registerPackageProvideTask,
      target
    )
  ) {
    dependsOn(PackageTaskName.CREATE.taskName(this@registerPackageProvideTask, target))
    outputs.file(packageFile)
    onlyIf { !packageFile.exists() }
  }
}

private fun XtrasLibrary.registerPackageExtractTask(target: KonanTarget) {
  if (taskInstallSource == null) return
  val libsDir = libsDir(target)
  val packageFile = packageFile(target)
  project.tasks.register<Exec>(
    PackageTaskName.EXTRACT.taskName(
      this@registerPackageExtractTask,
      target
    )
  ) {
    dependsOn(PackageTaskName.PROVIDE.taskName(this@registerPackageExtractTask, target))
    //onlyIf { !libsDir.exists() }
    inputs.file(packageFile)
    outputs.dir(libsDir)
    workingDir(libsDir)
    xtrasCommandLine("tar", "xpfz", packageFile)
  }
}

