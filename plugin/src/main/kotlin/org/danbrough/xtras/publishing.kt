package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.maven
import java.io.File

fun Publication.xtrasPom() {
  if (this is MavenPublication) {
    pom {

      name.set("Xtras")
      description.set("Kotlin support for common native libraries.")

      url.set("https://github.com/danbrough/xtras/")

      licenses {
        license {
          name.set("Apache-2.0")
          url.set("https://opensource.org/licenses/Apache-2.0")
        }
      }

      scm {
        connection.set("scm:git:git@github.com:danbrough/xtras.git")
        developerConnection.set("scm:git:git@github.com:danbrough/xtras.git")
        url.set("https://github.com/danbrough/xtras/")
      }

      issueManagement {
        system.set("GitHub")
        url.set("https://github.com/danbrough/xtras/issues")
      }

      developers {
        developer {
          id.set("danbrough")
          name.set("Dan Brough")
          email.set("dan@danbrough.org")
          organizationUrl.set("https://github.com/danbrough/xtras")
        }
      }
    }
  }
}

fun Project.xtrasDeclareXtrasRepository() {
  extensions.configure<PublishingExtension>("publishing") {
    repositories {
      findByName(XTRAS_REPO_NAME) ?: maven(xtrasMavenDir) {
        name = XTRAS_REPO_NAME
      }
    }
  }
}


val Project.xtrasLocalRepo: File
  get() = rootProject.layout.buildDirectory.file("mavenLocal").get().asFile

fun Project.xtrasDeclareLocalRepository() {
  extensions.configure<PublishingExtension>("publishing") {
    repositories {
      findByName(XTRAS_LOCAL_REPO_NAME) ?: maven(xtrasLocalRepo) {
        name = XTRAS_LOCAL_REPO_NAME
      }
    }
  }
}