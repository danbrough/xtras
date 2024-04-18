@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.xtrasMavenDir

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.xtras) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  `maven-publish`
}


val xtrasProjectGroup = XTRAS_PACKAGE
val xtrasProjectVersion: String = libs.versions.xtras.version.get()


allprojects {
  group = xtrasProjectGroup
  version = xtrasProjectVersion

  repositories {
    maven(xtrasMavenDir)
    maven("https://maven.danbrough.org")

    mavenCentral()
    google()
    //   maven("https://s01.oss.sonatype.org/content/groups/staging")
  }
}


tasks.register("thang") {
  val mavenID = "org.danbrough.openssl:binaries-openssl-mingwx64:3.3.0"
  actions.add {
    val configurationThang =
      project.configurations.create("configurationThang") {


      }

    project.dependencies {
      configurationThang(mavenID)
    }

    runCatching {
      configurationThang.resolvedConfiguration.files.all {
        println("RESOLIVED: $it")
        true
      }
    }.exceptionOrNull()?.also {
      println("ERROR: $it")
    }
  }
}