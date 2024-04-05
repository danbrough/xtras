pluginManagement {
  repositories {
    //maven("https://maven.danbrough.org")
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
  "mqtt",
  "ssh2",
  "jni",
).forEach {
  include(":libs:$it")
}

include(":examples")

//include(":iotest")