pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.xillio.com/artifactory/libs-release/")

  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "xtras"

includeBuild("plugin")

listOf(
  "support",
  "openssl",
  "ssh2",
).forEach {
  include(":libs:$it")
}

include(":iotest")