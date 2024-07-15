@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.XTRAS_GROUP

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.xtras)
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  `maven-publish`
}



allprojects {


  repositories {
    mavenCentral()
    google()
  }
}

