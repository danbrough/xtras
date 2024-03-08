pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    mavenCentral()
    gradlePluginPortal()
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