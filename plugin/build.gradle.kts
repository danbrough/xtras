
plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
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