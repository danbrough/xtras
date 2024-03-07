@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.projectProperty
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.xtras) apply false
  `maven-publish`
}


val xtrasProjectGroup = XTRAS_PACKAGE
val xtrasProjectVersion = libs.versions.xtras.version.get()


allprojects {
  group = xtrasProjectGroup
  version = xtrasProjectVersion

  repositories {
    mavenCentral()
  }
}


tasks.register("thang"){
  doFirst {
    println("EXTRA: ${project.projectProperty<Boolean?>("thang"){null}}")
  }
}