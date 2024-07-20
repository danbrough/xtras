import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  alias(libs.plugins.dokka)
  signing
  id("org.danbrough.xtras") version "0.0.1-beta01"
}


repositories {
  mavenCentral()
  google()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  withSourcesJar()
//  withJavadocJar()
}

kotlin {
  compilerOptions {
    this.jvmTarget = JvmTarget.JVM_11
  }
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  compileOnly(libs.dokka.gradle.plugin)
  compileOnly(libs.gradle.android)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = group.toString()
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}
