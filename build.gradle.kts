@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.konanDir
import org.danbrough.xtras.xtrasDir
import org.danbrough.xtras.xtrasLibsDir

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
    maven("https://maven.danbrough.org")
    //maven(xtrasPath(XtrasPath.MAVEN))
    mavenCentral()
    google()
    //   maven("https://s01.oss.sonatype.org/content/groups/staging")
  }
}


tasks.register<Exec>("thang") {
  doFirst {
    println("XTRAS DIR: $xtrasDir libs:$xtrasLibsDir")
  }
  commandLine("sh", "-c", "echo \"the date is `date`\"")
}