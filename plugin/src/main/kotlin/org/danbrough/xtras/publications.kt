package org.danbrough.xtras

import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.KonanTarget


fun XtrasLibrary.registerBinaryPublication(target: KonanTarget) {
  project.logInfo("registerBinaryPublications() $target")
  val publishing =
    project.extensions.findByType<PublishingExtension>() ?: project.apply("maven-publish").let {
      project.logWarn("applied the maven-publish plugin")
      project.extensions.findByType<PublishingExtension>()
    }
}
/*
fun LibraryExtension.registerPublications() {
  if (!buildEnabled) return
  project.logInfo("registerPublications()")
  project.extensions.findByType<PublishingExtension>()!!.run {
    supportedTargets.get().forEach { target ->
      val publicationName = "${this@registerPublications.name}${target.platformName.capitalized()}"

      publications.create<MavenPublication>(publicationName) {
        artifactId = artifactName(target)
        version = this@registerPublications.version
        groupId = this@registerPublications.group
        val artifactTask = project.tasks.getByName(taskNamePackageCreate(target))
        artifact(artifactTask.outputs.files.first()).builtBy(artifactTask)
        //println("PUBLICATION: artifactID:$artifactId group:$groupId version:$version")
      }
    }
  }
}
*/