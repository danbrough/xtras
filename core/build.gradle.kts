import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.xtrasDeclareXtrasRepository
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
  id("org.danbrough.xtras")
}

group = XTRAS_PACKAGE
version = "0.0.1-beta01"

repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.xtras.plugin)
}

xtrasDeclareXtrasRepository()