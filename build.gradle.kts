@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.XTRAS_PACKAGE

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
    mavenCentral()
    google()
  }
}

