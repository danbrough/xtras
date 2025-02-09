@file:Suppress("UnstableApiUsage")


plugins {

  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.dokka)
  signing
  `maven-publish`
  alias(libs.plugins.xtras)
}

group = "org.danbrough"

allprojects {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    google()
  }
}

