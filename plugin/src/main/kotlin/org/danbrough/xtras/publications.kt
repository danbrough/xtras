package org.danbrough.xtras

import org.danbrough.xtras.tasks.PackageTaskName
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


fun XtrasLibrary.registerBinaryPublication(target: KonanTarget) {
  val publishing = project.extensions.findByType<PublishingExtension>() ?: return

  val RESOLVEPackageTaskName = PackageTaskName.RESOLVE.taskName(this, target)
  val artifactTask = project.tasks.getByName(RESOLVEPackageTaskName)
  val publicationName = "${name}Binaries${target.kotlinTargetName.capitalized()}"

  publishing.publications.create<MavenPublication>(publicationName) {
    artifactId = artifactID(target)
    version = this@registerBinaryPublication.version
    groupId = this@registerBinaryPublication.group
    val file = artifactTask.outputs.files.first()
    //project.logError("registerBinaryPublication for file: ${file.absolutePath}")
    artifact(file).builtBy(artifactTask)
    pom {
      packaging = "tgz"
    }
  }
}

fun XtrasLibrary.resolveBinariesFromMaven(target: KonanTarget): File? {
  val mavenID = "$group:${artifactID(target)}:$version"
  project.logDebug("$name::resolveBinariesFromMaven():$target $mavenID")

  val binariesConfiguration =
    project.configurations.create("configuration${this@resolveBinariesFromMaven.name.capitalized()}Binaries${target.kotlinTargetName.capitalized()}") {
/*      isVisible = false
      isTransitive = false
      isCanBeConsumed = true
      isCanBeResolved = true*/
    }

  project.dependencies {
    binariesConfiguration(mavenID)
  }

  runCatching {
    return binariesConfiguration.resolve().first().also {
      project.logDebug("$name::resolveBinariesFromMaven(): $target found ${it.absolutePath}")
    }
  }.exceptionOrNull()?.let {
    project.logInfo("$name::resolveBinariesFromMaven():$target Failed for $mavenID: ${it.message}")
  }
  return null
}

