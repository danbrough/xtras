@file:Suppress("UnstableApiUsage")


plugins {

  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false

  signing
  `maven-publish`
  alias(libs.plugins.xtras) apply false
}


allprojects {
  repositories {
    mavenCentral()
    google()
  }
}

