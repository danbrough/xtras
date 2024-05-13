@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras.tasks

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.resolveBinariesFromMaven
import org.danbrough.xtras.unixPath

import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.environment
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

internal fun XtrasLibrary.registerPackageTasks(target: KonanTarget) {
  registerPackageCreateTask(target)
  registerPackageResolveTask(target)
  registerPackageExtractTask(target)
}

private fun XtrasLibrary.registerPackageCreateTask(target: KonanTarget) {

  project.tasks.register<Exec>(
    PackageTaskName.CREATE.taskName(
      this@registerPackageCreateTask,
      target
    )
  ) {
    group = XTRAS_TASK_GROUP
    environment(loadEnvironment())
    dependsOn(SourceTaskName.BUILD.taskName(this@registerPackageCreateTask, target))
    mustRunAfter(PackageTaskName.DOWNLOAD.taskName(this@registerPackageCreateTask, target))
    val packageFile = packageFile(target)
    onlyIf { !packageFile.exists() }
    inputs.dir(buildDir(target))
    workingDir(buildDir(target))
    outputs.file(packageFile)
    commandLine("sh","-c",
      "tar cvpfz ${project.unixPath(packageFile)} --exclude=**share --exclude=**pkgconfig ./"
    )
    doLast {
      sourceDir(target).deleteRecursively()
      buildDir(target).deleteRecursively()
    }
  }
}

private fun XtrasLibrary.registerPackageDownloadTask(target: KonanTarget) {
  val packageFile = packageFile(target)
  val taskName = PackageTaskName.DOWNLOAD.taskName(this@registerPackageDownloadTask, target)
  project.tasks.register(taskName) {
    group = XTRAS_TASK_GROUP
    onlyIf { !packageFile.exists() }
    actions.add {
      resolveBinariesFromMaven(target)?.also {
        project.logInfo("$name: resolved $it")
        it.copyTo(packageFile)
      }
    }

    outputs.file(packageFile)
  }
}

private fun XtrasLibrary.registerPackageResolveTask(target: KonanTarget) {
  registerPackageDownloadTask(target)
  val packageFile = packageFile(target)
  val taskName = PackageTaskName.RESOLVE.taskName(this@registerPackageResolveTask, target)

  project.tasks.register(taskName) {
    group = XTRAS_TASK_GROUP
    onlyIf { !packageFile.exists() }
    dependsOn(PackageTaskName.DOWNLOAD.taskName(this@registerPackageResolveTask, target))
    if (buildEnabled && !packageFile.exists())
      dependsOn(PackageTaskName.CREATE.taskName(this@registerPackageResolveTask, target))
    actions.add {
      if (!packageFile.exists()) {
        resolveBinariesFromMaven(target)?.copyTo(packageFile(target))
      }
    }

    outputs.file(packageFile)
  }
}

private fun XtrasLibrary.registerPackageExtractTask(target: KonanTarget) {

  val libsDir = libsDir(target)
  val packageFile = packageFile(target)
  val taskName = PackageTaskName.EXTRACT.taskName(this@registerPackageExtractTask, target)
  project.tasks.register<Exec>(taskName) {
    environment(loadEnvironment())
    group = XTRAS_TASK_GROUP
    doFirst {
      if (libsDir.exists()) libsDir.deleteRecursively()
      libsDir.mkdirs()
    }
    dependsOn(PackageTaskName.RESOLVE.taskName(this@registerPackageExtractTask, target))
    inputs.file(packageFile)
    outputs.dir(libsDir)
    workingDir(libsDir)
    commandLine("sh","-c","tar xpfz  ${project.unixPath(packageFile)}")
  }
}


