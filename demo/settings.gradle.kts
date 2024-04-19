
pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    //maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
}



rootProject.name = "xtras_demo"
