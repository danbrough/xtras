import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
  alias(libs.plugins.dokka)
  alias(libs.plugins.xtras)
}

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
  google()
}

group = "org.danbrough.openssl"

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
//  withJavadocJar()

}


dependencies {
  compileOnly(kotlin("gradle-plugin"))
  //noinspection UseTomlInstead
  //implementation("org.danbrough.xtras:plugin:0.0.1-beta12")
  compileOnly(libs.xtras.plugin)

  //compileOnly("com.android.tools.build:gradle:8.5.2")
  compileOnly(libs.gradle.android)
}


gradlePlugin {
  plugins {
    create("ssh2") {
      id = group.toString()
      implementationClass = "$group.SSH2Plugin"
      displayName = "SSH2 Plugin"
      description = "Kotlin multiplatform support plugin for libssh2"
    }
  }
}




