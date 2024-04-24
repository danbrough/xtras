pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "xtras"

includeBuild("plugin")
includeBuild("core")


listOf(
  "logging",
  "support",
  "openssl",
  "ssh2",
).forEach {
  include(":libs:$it")
}



