package org.danbrough.xtras

import org.danbrough.xtras.tasks.taskNamePackageCreate
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType

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