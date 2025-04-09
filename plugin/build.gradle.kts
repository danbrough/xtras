import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  //`java-gradle-plugin`
  `maven-publish`
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  //compileOnly(libs.gradle.android)
}

group = "org.danbrough.xtras"
version = "0.0.1"

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
//  withJavadocJar()

}

kotlin {
  //println("KOTLIN_PLUGIN_VERSION: ${this.coreLibrariesVersion}")
  compilerOptions.jvmTarget = JvmTarget.JVM_11
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

gradlePlugin {
  plugins {
    create("xtrasSettings") {
      id = "org.danbrough.xtras.settings"
      implementationClass = "$group.XtrasSettingsPlugin"
      displayName = "Xtras Settings Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}