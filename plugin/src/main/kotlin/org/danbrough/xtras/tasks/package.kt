package org.danbrough.xtras.tasks

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.platformName
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

internal fun LibraryExtension.registerPackageTasks() {
  supportedTargets.get().forEach {
    if (buildEnabled)
      registerPackageCreateTask(it)

    registerPackageExtractTask(it)
    registerPackageDownloadTask(it)
    registerPackageProvideTask(it)
  }
}

private fun LibraryExtension.registerPackageProvideTask(target: KonanTarget) {
  project.tasks.register(taskNamePackageProvide(target)) {
    dependsOn(taskNamePackageDownload(target))
    if (buildEnabled) dependsOn(taskNamePackageCreate(target))
    val packageFile = packageFile(target)
    onlyIf {
      project.forceBuild() || !packageFile.exists()
    }
    outputs.file(packageFile)
  }
}

private fun LibraryExtension.registerPackageCreateTask(target: KonanTarget) {
  project.tasks.register<Exec>(taskNamePackageCreate(target)) {
    mustRunAfter(taskNamePackageDownload(target))
    dependsOn(
      xtrasTaskName(
        TASK_GROUP_SOURCE,
        SourceTaskName.INSTALL.name.lowercase(),
        this@registerPackageCreateTask,
        target
      )
    )
    val archiveFile = packageFile(target)
    onlyIf {
      project.forceBuild() || !archiveFile.exists()
    }
    group = XTRAS_TASK_GROUP
    workingDir(buildDir(target))
    outputs.file(archiveFile)
    commandLine(
      xtras.buildEnvironment.binaries.tar,
      "cvpfz",
      archiveFile.absolutePath,
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

private fun LibraryExtension.registerPackageExtractTask(target: KonanTarget) {
  project.tasks.register<Exec>(taskNamePackageExtract(target)) {
    if (buildEnabled) {
      dependsOn(
        xtrasTaskName(
          TASK_GROUP_PACKAGE,
          PackageTaskName.CREATE.name.lowercase(),
          this@registerPackageExtractTask,
          target
        )
      )
    }
    val packageFile = packageFile(target)
    val libsDir = libsDir(target)
    group = XTRAS_TASK_GROUP
    workingDir(libsDir(target))
    inputs.file(packageFile)
    outputs.dir(libsDir)

    onlyIf {
      project.forceBuild() || !libsDir.exists()
    }

    doFirst {
      project.logDebug("extracting archive ${packageFile.absolutePath} to ${libsDir.absolutePath}")
      //libsDir.deleteRecursively()
      project.mkdir(libsDir)
      project.logDebug("running command ${commandLine.joinToString(" ")}")
    }

    commandLine(xtras.buildEnvironment.binaries.tar, "xpfz", packageFile.absolutePath)

  }
}

private fun LibraryExtension.registerPackageDownloadTask(target: KonanTarget) =
  project.tasks.register(taskNamePackageDownload(target)) {
    val packageFile = packageFile(target)
    onlyIf {
      project.forceBuild() || !packageFile.exists()
    }
    group = XTRAS_TASK_GROUP
    actions.add {
      resolveBinariesFromMaven(target)?.also {
        project.logDebug("copying ${it.absolutePath} to ${packageFile.absolutePath}")
        it.copyTo(packageFile, true)
      }
    }
    outputs.file(packageFile)
  }

private fun LibraryExtension.resolveBinariesFromMaven(target: KonanTarget): File? {
  val mavenID = "${artifactName(target)}:$version"
  project.logDebug("LibraryExtension.resolveBinariesFromMaven():$target $mavenID")

  val binariesConfiguration =
    project.configurations.create("configuration${this@resolveBinariesFromMaven.name.capitalized()}Binaries${target.platformName.capitalized()}") {
      isVisible = false
      isTransitive = false
      isCanBeConsumed = false
      isCanBeResolved = true
    }

  /*  project.repositories.all {
      if (this is MavenArtifactRepository) {
        project.logDebug("LibraryExtension.resolveBinariesFromMaven():$target REPO: ${this.name}:${this.url}")
      }
    }*/

  project.dependencies {
    binariesConfiguration(mavenID)
  }

  runCatching {
    binariesConfiguration.resolve().first().also {
      project.logDebug("LibraryExtension.resolveBinariesFromMaven():$target found ${it.absolutePath}")
    }
  }.exceptionOrNull()?.let {
    project.logInfo("LibraryExtension.resolveBinariesFromMaven():$target Failed for $mavenID: ${it.message}")
  }
  return null
}

