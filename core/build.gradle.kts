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

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
//  withJavadocJar()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.xtras.plugin)
}

java {
  withSourcesJar()
}
