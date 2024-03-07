
pluginManagement {
  repositories {
    //maven("/home/dan/workspace/xtras/xtras/maven")
maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}



rootProject.name = "xtras_demo"
