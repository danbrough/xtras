import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  alias(libs.plugins.gradle.publish)
//  `java-gradle-plugin`
//  `maven-publish`
//  signing
  //alias(libs.plugins.dokka)
  //alias(libs.plugins.xtras)
}

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
  google()
}

group = "org.danbrough.openssl"
version = "0.0.1-alpha1"

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
//  withJavadocJar()

}


dependencies {
  compileOnly(kotlin("gradle-plugin"))
  //compileOnly(libs.xtras.plugin)
  compileOnly("org.danbrough.xtras:plugin")
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


tasks.register("thang2"){
  doFirst {
    //project.xtrasLogger.info("hello from thang2!")
    println("thang2!")
  }
}

