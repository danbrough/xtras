plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
}

group = libs.versions.xtrasPackage.get()
version = libs.versions.xtras.version.get()

repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  compileOnly(libs.dokka.gradle.plugin)

  compileOnly(libs.gradle.android)
}

java {
  withSourcesJar()
  withJavadocJar()
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }

    create("sonatype") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
      displayName = "Xtras Sonatype Plugin"
      description = "Sonatype support for Xtras projects"
    }
  }
}


fun extraProperty(name: String): String? = if (extra.has(name)) extra[name]?.toString() else null

val sonatypeRepoId = extraProperty("sonatype.repoID")

signing {
  sign(publishing.publications)
}

publishing {

  repositories {
    val xtrasMavenDir =
      if (hasProperty("xtras.dir.maven")) File(property("xtras.dir.maven").toString())
      else if (hasProperty("xtras.dir")) File(property("xtras.dir").toString()).resolve("maven")
      else error("Neither xtras.dir.maven or xtras.dir are set")

    logger.info("xtrasMavenDir = $xtrasMavenDir")

    maven(xtrasMavenDir) {
      name = "Xtras"
    }

    if (sonatypeRepoId != null) {
      maven("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$sonatypeRepoId") {
        name = "Sonatype"
        credentials {
          username = extraProperty("sonatype.username")
          password = extraProperty("sonatype.password")
        }
      }
    }
  }
}

publishing.publications.all {
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

