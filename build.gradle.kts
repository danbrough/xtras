@file:Suppress("UnstableApiUsage")


plugins {

  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.dokka) apply false


  signing
  `maven-publish`
  alias(libs.plugins.xtras)
}


allprojects {
  repositories {
    //maven("https://maven.danbrough.org")
    //maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    google()
  }
}

