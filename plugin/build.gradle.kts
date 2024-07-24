import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  alias(libs.plugins.dokka)
  signing
  id("org.danbrough.xtras") version "0.0.1-beta03"
}

version = "0.0.1-beta04"

repositories {
  mavenCentral()
  google()
}

java {
  //sourceCompatibility = JavaVersion.VERSION_1_8
  //targetCompatibility = JavaVersion.VERSION_1_8
  withSourcesJar()
//  withJavadocJar()
}

kotlin {
  compilerOptions {
//    this.jvmTarget = JvmTarget.JVM_1_8
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
