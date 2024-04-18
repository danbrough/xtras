package org.danbrough.xtras.sonatype

import org.danbrough.xtras.XTRAS_REPO_NAME
import org.danbrough.xtras.XtrasPath
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.xtrasPath
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File


fun PublishingExtension.sonatypeStaging() {
  val stagingRepoName = "SonatypeStaging"
  repositories.findByName(stagingRepoName)
    ?: repositories.maven("https://s01.oss.sonatype.org/content/groups/staging") {
      name = stagingRepoName
    }
}

internal fun Project.configurePublishing() {
  logInfo("configurePublishing()")
  apply<MavenPublishPlugin>()
  apply<SigningPlugin>()
  val sonatype = extensions.create<SonatypeExtension>(SonatypeExtension.EXTENSION_NAME)

  extensions.configure<SonatypeExtension> {
    urlBase.convention("https://s01.oss.sonatype.org")
    repoID.convention(projectProperty(SonatypeExtension.REPO_ID) { "" })
    profileID.convention(projectProperty(SonatypeExtension.PROFILE_ID) { "" })
    username.convention(projectProperty(SonatypeExtension.USERNAME) { "" })
    password.convention(projectProperty(SonatypeExtension.PASSWORD) { "" })
    description.convention(projectProperty(SonatypeExtension.DESCRIPTION) { "" })
  }

  afterEvaluate {

    createOpenRepoTask(sonatype)
    createCloseRepoTask(sonatype)

    extensions.findByType<PublishingExtension>()!!.run {
      extensions.findByType<KotlinMultiplatformExtension>()?.run {


        val emptyFileForJavadocTaskName = "emptyFileForJavadoc"

        val emptyFileTask: Task =
          project.tasks.findByName(emptyFileForJavadocTaskName) ?: project.tasks.create(
            emptyFileForJavadocTaskName
          ) {
            val outputFile =
              File(System.getProperty("java.io.tmpdir"), "emptyFileForJavadoc_${project.path.replace(':','_')}")
            actions.add {
              outputFile.writeText("Empty file for javadocs")
            }
            outputs.file(outputFile)
          }

        val emptyJavadocsTask = "emptyJavadocs"
        val emptyJarTask: Task =
          project.tasks.findByName(emptyJavadocsTask)
            ?: project.tasks.create<Jar>(emptyJavadocsTask) {
              archiveClassifier.set("javadoc")
              from(emptyFileTask)
              dependsOn(emptyFileForJavadocTaskName)
            }


        publications.withType<MavenPublication> {
          //logTrace("PUBLICATION: project: ${project.name} name:$name type:${this::class.java}")
          if (!setOf("kotlinMultiplatform", "jvm").contains(name)) {
            //logWarn("ADDING EMPTY JAVADOC to MAVEN PUBLICATION $name")
            artifact(emptyJarTask.outputs.files.first()).builtBy(emptyJarTask)
          }
        }
        val signTasks = tasks.withType<Sign>()
        val jarTasks = tasks.withType<Jar>()

        tasks.withType(PublishToMavenRepository::class.java) {
          if (signTasks.isNotEmpty())
            dependsOn(signTasks)
          if (jarTasks.isNotEmpty())
            dependsOn(jarTasks)
        }
      }


      extensions.findByType<SigningExtension>()!!.run {
        publications.all {
          sign(this)
        }
      }

      repositories.findByName(XTRAS_REPO_NAME)
        ?: repositories.maven(project.xtrasPath(XtrasPath.MAVEN)) {
          name = XTRAS_REPO_NAME
        }

      val repoID = sonatype.repoID.get()


      //val publishingURL =
      //if (sonatype.son) "${extn.sonatypeUrlBase}/content/repositories/snapshots/" else
      //if (sonatype.repoID.get().isNotBlank()) "${sonatype.urlBase.get()}/service/local/staging/deployByRepositoryId/${extn.sonatypeRepoId}"
      //else "${extn.sonatypeUrlBase}/service/local/staging/deploy/maven2/"

      if (repoID != "") {
        repositories.findByName(SonatypeExtension.REPO_NAME)
          ?: repositories.maven("${sonatype.urlBase.get()}/service/local/staging/deployByRepositoryId/${repoID}") {
            name = SonatypeExtension.REPO_NAME
            credentials {
              username = sonatype.username.get()
              password = sonatype.password.get()
            }
          }
      }
    }


  }
}


/*



//import org.jetbrains.dokka.gradle.DokkaTask


fun Project.sonatypePublishing(block: SonatypeExtension.() -> Unit = {}) {
  sonatype {
    block()
    createOpenRepoTask(this)
    if (sonatypeRepoId == null) {
      logDebug("sonatypeRepoId is not specified")
      return@sonatype
    }
    createCloseRepoTask(this)
    configurePublishing(this)
  }
}


fun Project.sonatype(block: SonatypeExtension.() -> Unit = {}) {

  extensions.findByName("sonatype") ?: extensions.create<SonatypeExtension>("sonatype", this)
  extensions.configure<SonatypeExtension>("sonatype") {
    block()
  }
}

internal fun Project.configurePublishing(extn: SonatypeExtension) {

  extensions.configure<PublishingExtension>("publishing") {
    sonatype {
      project.log("Project.configurePublishing - ${project.group}:${project.name}:${project.version}")



      extn.configurePublishing(this@configure, this@configurePublishing)

      val publishingURL =
        if (extn.sonatypeSnapshot) "${extn.sonatypeUrlBase}/content/repositories/snapshots/"
        else if (!extn.sonatypeRepoId.isNullOrBlank()) "${extn.sonatypeUrlBase}/service/local/staging/deployByRepositoryId/${extn.sonatypeRepoId}"
        else "${extn.sonatypeUrlBase}/service/local/staging/deploy/maven2/"


      project.log("SonatypeExtension::publishingURL $publishingURL repoID is: ${extn.sonatypeRepoId}")

      if (extn.publishDocs && plugins.hasPlugin("org.jetbrains.dokka")) {
        tasks.named<DokkaTask>("dokkaHtml").configure {
          outputDirectory.set(project.xtrasDocsDir)
        }

        val javadocJar by tasks.registering(Jar::class) {
          archiveClassifier.set("javadoc")
          from(tasks.named<DokkaTask>("dokkaHtml"))
        }

        publications.all {
          if (this is MavenPublication) artifact(javadocJar)
        }
      }


      extensions.findByType<KotlinProjectExtension>()?.apply {
        sourceSets.findByName("main")?.kotlin?.also { srcDir ->
          val sourcesJarTask = tasks.register("sourcesJar${
            name.replaceFirstChar {
              if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
              ) else it.toString()
            }
          }", Jar::class.java) {
            archiveClassifier.set("sources")
            from(srcDir)
          }

          publications.all {
            if (this is MavenPublication) artifact(sourcesJarTask)
          }
        }
      }


      if (extn.signPublications) {
        apply<SigningPlugin>()
        extensions.getByType<SigningExtension>().apply {
          publications.all {
            sign(this)
          }
        }
      } else {
        log("extn.signPublications is false")
      }

      val signTasks = tasks.withType(Sign::class.java).map { it.name }
      if (signTasks.isNotEmpty()) {
        tasks.withType(PublishToMavenRepository::class.java) {
          dependsOn(signTasks)
        }
      }


      repositories {
        maven {
          name = "SonaType"
          url = URI(publishingURL)
          credentials {
            username = extn.sonatypeUsername
            password = extn.sonatypePassword
          }
        }


      }

    }
  }
}

 */