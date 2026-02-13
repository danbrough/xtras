@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskNames.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.resolveBinariesFromMaven
import org.danbrough.xtras.taskNamePackage
import org.danbrough.xtras.taskNamePackageDownload
import org.danbrough.xtras.taskNamePackageResolve
import org.danbrough.xtras.xInfo
import org.jetbrains.kotlin.konan.target.KonanTarget

/*internal fun XtrasLibrary.registerPackageTasks(target: KonanTarget) {
  registerPackageCreateTask(target)
  registerPackageResolveTask(target)
  registerPackageExtractTask(target)
}*/



private fun XtrasLibrary.registerPackageDownloadTask(target: KonanTarget) {
  val packageFile = packageFileMap(target)
  project.tasks.register(taskNamePackageDownload(target)) {
    group = XTRAS_TASK_GROUP
    doFirst {
      xInfo("XtrasLibrary[$name]::registerPackageDownloadTask($target) packageFile: $packageFile exists: ${packageFile.exists()}")
    }
    onlyIf { !packageFile.exists() }
    actions.add {
      resolveBinariesFromMaven(target)?.also {
        xInfo("$name: resolved $it")
        it.copyTo(packageFile)
      }
    }

    outputs.file(packageFile)
  }
}

fun XtrasLibrary.registerPackageResolveTask(target: KonanTarget) {
  registerPackageDownloadTask(target)
  val packageFile = packageFileMap(target)

  project.tasks.register(taskNamePackageResolve(target)) {
    group = XTRAS_TASK_GROUP
    onlyIf { !packageFile.exists() }
    dependsOn(taskNamePackageDownload(target))
    if (!packageFile.exists())
      dependsOn(taskNamePackage(target))
    actions.add {
      if (!packageFile.exists()) {
        resolveBinariesFromMaven(target)?.copyTo(packageFile)
      }
    }

    outputs.file(packageFile)
  }
}



