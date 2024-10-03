import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  alias(libs.plugins.dokka)
  signing
  id("org.danbrough.xtras") version "0.0.1-beta11"
}

group = "org.danbrough.xtras"

repositories {
  //maven("file:///files/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
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
    jvmTarget = JvmTarget.JVM_11
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
      id = "org.danbrough.xtras"
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}
