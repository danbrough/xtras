package org.danbrough.xtras

import org.danbrough.xtras.Xtras.Constants.SONATYPE_REPO_NAME
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.io.File
import java.net.URI

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

internal fun Project.withPublishing(block: PublishingExtension.() -> Unit) {
  //findProperty("publishing") ?: apply<MavenPublishPlugin>()
  runCatching {
    extensions.findByType<PublishingExtension>()?.block()
  }.exceptionOrNull()?.also {
    logError("FAILED withPublishing!! ", it)
  }
}

private fun Project.xtrasPublishToXtras() = registerPublishRepo(XTRAS_REPO_NAME, xtrasMavenDir)

val Project.xtrasLocalRepoDir: File
  get() = rootProject.layout.buildDirectory.file("m2").get().asFile

private fun Project.xtrasPublishToLocal() =
  registerPublishRepo(XTRAS_LOCAL_REPO_NAME, xtrasLocalRepoDir)

private fun Project.xtrasPublishToSonatype() {

  logInfo("$name::xtrasPublishToSonatype")

  registerSonatypeTasks()

  val baseURL =
    xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_BASE_URL) { "https://s01.oss.sonatype.org" }

  val snapshot = xtrasProperty(Xtras.Constants.Properties.SONATYPE_SNAPSHOT) { false }

  val openRepository = xtrasProperty(Xtras.Constants.Properties.SONATYPE_OPEN_REPOSITORY) { true }

  val closeRepository =
    xtrasProperty(Xtras.Constants.Properties.SONATYPE_CLOSE_REPOSITORY) { false }


//  val publishing =
//    extensions.findByType<PublishingExtension>() ?: error("PublishingExtension not found")
  withPublishing {
    repositories {
      maven {
        name = SONATYPE_REPO_NAME
        credentials {
          username =
            xtrasProperty(Xtras.Constants.Properties.SONATYPE_USERNAME) { error("${Xtras.Constants.Properties.SONATYPE_USERNAME} not specified in gradle.properties") }
          password =
            xtrasProperty(Xtras.Constants.Properties.SONATYPE_PASSWORD) { error("${Xtras.Constants.Properties.SONATYPE_PASSWORD} not specified in gradle.properties") }
        }
      }
    }
  }


  afterEvaluate {
    logInfo("xtrasPublishToSonatype: openRepository: $openRepository closeRepository: $closeRepository snapshot:$snapshot")

    tasks.withType<PublishToMavenRepository>().filter { it.repository?.name == SONATYPE_REPO_NAME }
      .forEach { publishTask ->
        if (openRepository) publishTask.dependsOn(Xtras.Constants.TaskNames.SONATYPE_OPEN_REPO)
        if (closeRepository)
          publishTask.finalizedBy(Xtras.Constants.TaskNames.SONATYPE_CLOSE_REPO)
        publishTask.doFirst {

          withPublishing {
            repositories.getByName(SONATYPE_REPO_NAME)
              .apply {
                this as MavenArtifactRepository
                val repoID = sonatypeRepoIDFile.exists().let {
                  if (it) sonatypeRepoIDFile.readText()
                    .trim() else error("Failed read repoID from $sonatypeRepoIDFile")
                }

                val sonatypeURL =
                  if (snapshot) "$baseURL/content/repositories/snapshots/"
                  else
                    if (repoID != "") "$baseURL/service/local/staging/deployByRepositoryId/$repoID" else
                      "$baseURL/service/local/staging/deploy/maven2/"


                logWarn("sonatype:publish url: $sonatypeURL")

                this.url = URI.create(sonatypeURL)
              }
          }
        }
      }
  }
}

private fun Project.registerPublishRepo(repoName: String, url: Any) {
  withPublishing {
    repositories {
      maven(url) {
        name = repoName
      }
    }
  }
}


internal fun Project.xtrasPublishing() {
  apply<MavenPublishPlugin>()
  apply<SigningPlugin>()

  withPublishing {
    publications.all {
      xtrasPom(
        projectName = xtrasProperty(Xtras.Constants.Properties.PROJECT_NAME) { project.name },
        projectDescription = xtrasProperty(Xtras.Constants.Properties.PROJECT_DESCRIPTION) {
          project.description.also {
            logWarn("${Xtras.Constants.Properties.PROJECT_DESCRIPTION} should be set instead of ${this@xtrasPublishing.name}.description")
          } ?: ""
        }
      )
    }
  }

  val signPublications = xtrasProperty(Xtras.Constants.Properties.PUBLISH_SIGN) { true }

  if (signPublications) {
    logTrace("configuring signing..")
    extensions.configure<SigningExtension> {

      val signingKey =
        xtrasProperty<String>(Xtras.Constants.Properties.SIGNING_KEY) { error("${Xtras.Constants.Properties.SIGNING_KEY} not set") }.replace(
          "\\n",
          "\n"
        )
      val signingPassword =
        xtrasProperty<String>(Xtras.Constants.Properties.SIGNING_PASSWORD) { error("${Xtras.Constants.Properties.SIGNING_PASSWORD} not set") }

      useInMemoryPgpKeys(signingKey, signingPassword)

      withPublishing {
        sign(publications)
      }
    }
  }

  if (xtrasProperty<Boolean>(Xtras.Constants.Properties.PUBLISH_DOCS) { true }) {
    logTrace("configuring docs..")
    pluginManager.apply("org.jetbrains.dokka")
    val javadocTask = tasks.register<Jar>("javadocJar") {

      group = JavaBasePlugin.DOCUMENTATION_GROUP
      archiveClassifier.set("javadoc")
      //from(tasks.getByName("dokkaHtml"))

      from(tasks.getByName("dokkaGenerateModuleHtml"))
      //from(tasks.getByName("dokkaGeneratePublicationHtml"))

    }


    withPublishing {
      publications.all {
        if (this is MavenPublication) {
          artifact(javadocTask)
        }
      }

      val signingTasks = tasks.withType<Sign>()
      tasks.withType<PublishToMavenRepository> {
        mustRunAfter(signingTasks)
      }

    }
  }

  xtrasPublishToLocal()

  xtrasPublishToXtras()

  xtrasPublishToSonatype()

}

