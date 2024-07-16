import org.danbrough.xtras.XTRAS_GROUP
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.xtras)
}

repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.xtras.plugin)
}

