import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  alias(libs.plugins.gradle.publish)
//  `java-gradle-plugin`
//  `maven-publish`
//  signing
  //alias(libs.plugins.dokka)
  alias(libs.plugins.xtras)
}


group = "org.danbrough.openssl"
version = "0.0.1-alpha1"

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
//  withJavadocJar()
}

kotlin {
  compilerOptions.jvmTarget = JvmTarget.JVM_11
}

dependencies {
  compileOnly(kotlin("gradle-plugin"))
//  compileOnly("org.danbrough.xtras:plugin")
  implementation(libs.xtras.plugin)

  //implementation("org.danbrough.xtras:plugin:0.0.1-beta12")
  //compileOnly(libs.xtras.plugin)

  //compileOnly("com.android.tools.build:gradle:8.5.2")
}


gradlePlugin {
  plugins {
    create("openssl") {
      id = group.toString()
      implementationClass = "$group.plugin.OpenSSLPlugin"
      displayName = "OpenSSL Plugin"
      description = "Kotlin multiplatform support plugin for openssl"
    }
  }
}


tasks.register("thang2") {
  doFirst {

  }
}

