package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.maven
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.io.File

fun Publication.xtrasPom(
  projectName: String,
  projectDescription: String,
  githubAccount: String = "danbrough",
  website: String = "https://github.com/$githubAccount/$projectName",
  issuesSite: String? = "$website/issues",
  scmSite: String? = "scm:git:git@github.com:$githubAccount/$projectName.git",
  licenseApache2: Boolean = true,
  block: MavenPom.() -> Unit = {},
) {
  if (this is MavenPublication) {
    pom {

      name.set(projectName)
      description.set(projectDescription)

      url.set(website)

      licenses {
        if (licenseApache2)
          license {
            name.set("Apache-2.0")
            url.set("https://opensource.org/licenses/Apache-2.0")
          }
      }

      scm {
        connection.set(scmSite)
        developerConnection.set(scmSite)
        url.set(website)
      }

      if (issuesSite != null)
        issueManagement {
          system.set("GitHub")
          url.set(issuesSite)
        }

      developers {
        developer {
          id.set("danbrough")
          name.set("Dan Brough")
          email.set("dan@danbrough.org")
          organizationUrl.set(website)
        }
      }
      block()
    }
  }
}

private fun Project.withPublishing(block: PublishingExtension.() -> Unit) {
  //findProperty("publishing") ?: apply<MavenPublishPlugin>()
  extensions.configure<PublishingExtension>("publishing", block)
}

private fun Project.xtrasPublishToXtras() = registerPublishRepo(XTRAS_REPO_NAME, xtrasMavenDir)

val Project.xtrasLocalRepoDir: File
  get() = rootProject.layout.buildDirectory.file("m2").get().asFile

private fun Project.xtrasPublishToLocal() =
  registerPublishRepo(XTRAS_LOCAL_REPO_NAME, xtrasLocalRepoDir)

private fun Project.xtrasPublishToSonatype() {

  fun MavenArtifactRepository.configureCredentials() {
    credentials {
      username =
        xtrasProperty(Xtras.SONATYPE_USERNAME) { error("${Xtras.SONATYPE_USERNAME} not specified in gradle.properties") }
      password =
        xtrasProperty(Xtras.SONATYPE_PASSWORD) { error("${Xtras.SONATYPE_PASSWORD} not specified in gradle.properties") }
    }
  }

  withPublishing {
    repositories {

      maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "Sonatype"
        configureCredentials()
      }

      maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "Snapshots"
        configureCredentials()
      }
    }
  }
}
//
//fun Project.xtrasConfigureSigning() {
//  withPublishing {
//    findProperty("signing") ?: pluginManager.apply("signing")
//    extensions.configure<SigningExtension> {
//      publications.all {
//        sign(this)
//      }
//    }
//  }
//}

private fun Project.registerPublishRepo(repoName: String, url: Any) {
  withPublishing {
    repositories {
      maven(url) {
        name = repoName
      }
    }
  }
}


internal fun Project.xtrasPublishing(xtras: Xtras) {

  if (xtrasProperty<Boolean>(Xtras.PUBLISH_LOCAL) { false }) {
    xtrasPublishToLocal()
  }

  if (xtrasProperty<Boolean>(Xtras.PUBLISH_XTRAS) { false }) {
    xtrasPublishToXtras()
  }

  if (xtrasProperty<Boolean>(Xtras.PUBLISH_SONATYPE) { false }) {
    //project.extra[Xtras.PUBLISH_SIGN] = "true"
    xtrasPublishToSonatype()
  }

  withPublishing {
    publications.all {
      xtrasPom(
        projectName = xtrasProperty(Xtras.PROJECT_NAME) { project.name },
        projectDescription = xtrasProperty(Xtras.PROJECT_DESCRIPTION) {
          project.description.also {
            logWarn("${Xtras.PROJECT_DESCRIPTION} should be set instead of ${this@xtrasPublishing.name}.description")
          } ?: ""
        }
      )
    }
  }

  if (xtrasProperty(Xtras.PUBLISH_SIGN) { false }) {
    logTrace("configuring signing..")
    pluginManager.apply(SigningPlugin::class)
    extensions.configure<SigningExtension> {

      val signingKey =
        xtrasProperty<String>(Xtras.SIGNING_KEY) { error("${Xtras.SIGNING_KEY} not set") }.replace(
          "\\n",
          "\n"
        )
      val signingPassword =
        xtrasProperty<String>(Xtras.SIGNING_PASSWORD) { error("${Xtras.SIGNING_PASSWORD} not set") }

      useInMemoryPgpKeys(signingKey, signingPassword)

      withPublishing {
        publications.all {
          sign(this)
        }
      }
    }
  }

  if (xtrasProperty(Xtras.PUBLISH_DOCS) { false }) {
    logTrace("configuring docs..")
    pluginManager.apply("org.jetbrains.dokka")
    val javadocTask = tasks.register("javadocJar", Jar::class.java) {
      archiveClassifier.set("javadoc")
      from(tasks.getByName("dokkaHtml"))
    }

    afterEvaluate {
      withPublishing {
        publications.all {
          if (this is MavenPublication) {
            artifact(javadocTask)
          }
        }
      }

      afterEvaluate {
        val signTasks = tasks.withType(Sign::class.java).map { it.name }
        if (signTasks.isNotEmpty()) {
          tasks.withType(PublishToMavenRepository::class.java) {
            //  println("$name => $signTasks")
            dependsOn(signTasks)
          }
        }
      }
    }

    tasks.getByName("dokkaHtml").doFirst {
      println("RUNNING DOKKA HTML FOR PROJECT: ${this@xtrasPublishing.name}")
    }


    /*    val javadocJar by tasks.registering(Jar::class) {
    //      archiveClassifier.set("javadoc")
    //      from(tasks.getByName("dokkaHtml"))
        }*/

  }

  /*
afterEvaluate {
val signTasks = tasks.withType(Sign::class.java).map { it.name }
if (signTasks.isNotEmpty()) {
tasks.withType(PublishToMavenRepository::class.java) {
  //println("$name => $signTasks")
  dependsOn(signTasks)
}
}
}
 */
}

