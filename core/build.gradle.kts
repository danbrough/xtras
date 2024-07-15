import org.danbrough.xtras.XTRAS_GROUP
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
  id("org.danbrough.xtras")
}



repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.xtras.plugin)
}

