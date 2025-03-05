
plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  //compileOnly(libs.gradle.android)
}

group = "org.danbrough.xtras"

gradlePlugin {
  plugins {
    create("xtras2") {
      id = "org.danbrough.xtras2"
      implementationClass = "$group.XtrasPlugin2"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}