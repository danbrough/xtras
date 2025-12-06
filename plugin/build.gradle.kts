import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  //`java-gradle-plugin`
  `maven-publish`
  signing
  //id("org.danbrough.xtras") version "0.0.1-beta14"
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  //compileOnly(libs.gradle.android)
}

group = "org.danbrough.xtras"
version = "0.0.2-alpha01"

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
//  withJavadocJar()

}

kotlin {
  //println("KOTLIN_PLUGIN_VERSION: ${this.coreLibrariesVersion}")
  compilerOptions.jvmTarget = JvmTarget.JVM_11
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}

gradlePlugin {
  plugins {
    create("xtrasSettings") {
      id = "org.danbrough.xtras.settings"
      implementationClass = "$group.XtrasSettingsPlugin"
      displayName = "Xtras Settings Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}

val xtrasDir: String = project.properties["xtras.dir"]?.toString()
  ?: error("xtras.dir should be set to a directory for generated files")

val xtrasMavenDir: String =
  project.properties["xtras.dir.maven"]?.toString() ?: File(xtrasDir, "maven").absolutePath


publishing {
  repositories {
//    maven(rootProject.layout.buildDirectory.dir("maven")) {
    maven(rootProject.layout.buildDirectory.file("m2").get().asFile) {
      name = "xtras"
    }
  }


  signing {
    val signingKey = project.properties["signing.key"].toString().replace(
      "\\n", "\n"
    )
    val signingPassword = project.properties["signing.password"].toString()

    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publications)

    /*
          val signingKey =
          xtrasPropertyValue<String>(SIGNING_KEY) { error("$SIGNING_KEY not set") }.replace(
            "\\n", "\n"
          )
        val signingPassword =
          xtrasPropertyValue<String>(SIGNING_PASSWORD) { error("$SIGNING_PASSWORD not set") }

        useInMemoryPgpKeys(signingKey, signingPassword)

        withPublishing {
          sign(publications)
        }
     */

    publications.all {
      val projectName = project.name
      val projectDescription = "Xtras gradle plugin"
      val licenseApache2 = true
      val githubAccount: String = "danbrough"
      val website: String = "https://github.com/$githubAccount/$projectName"
      val issuesSite: String = "$website/issues"
      val scmSite: String = "scm:git:git@github.com:$githubAccount/$projectName.git"
      if (this is MavenPublication) {
        pom {

          name.set(projectName)
          description.set(projectDescription)

          url.set(website)

          licenses {
            if (licenseApache2) license {
              name.set("Apache-2.0")
              url.set("https://opensource.org/licenses/Apache-2.0")
            }
          }

          scm {
            connection.set(scmSite)
            developerConnection.set(scmSite)
            url.set(website)
          }

          if (issuesSite != null) issueManagement {
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
        }
      }
    }
  }
}