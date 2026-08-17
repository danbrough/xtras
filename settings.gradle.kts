@file:Suppress("UnstableApiUsage")

pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    maven("file:///files/cache/xtras/maven")
    mavenCentral()
    google()
    gradlePluginPortal()
    //maven("https://s01.oss.sonatype.org/content/groups/staging/")
  }
}

dependencyResolutionManagement {
  repositories {
    maven("https://maven.danbrough.org")
    maven("file:///files/cache/xtras/maven")
    //maven("https://s01.oss.sonatype.org/content/groups/staging")
    google()
    mavenCentral()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.6"
}

rootProject.name = "xtras"

//includeBuild("plugin")
includeBuild("plugin")


val pluginOnly: String? by settings

val libs = listOf(
  "openssl",
  //"ssh2",
  //"curl",
  "sqlite",
)

if (pluginOnly == null) {
  libs.forEach {
    include(":$it")
    includeBuild("libs/$it/plugin") {
      name = "${it}_plugin"
    }
    project(":$it").projectDir = file("libs/$it")
  }

  //include(":test2")
  include(":support")
  project(":support").projectDir = file("libs/support")


}



