plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.dokka)
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

version = libs.versions.xtras.publish.get()
