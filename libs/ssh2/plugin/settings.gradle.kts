@file:Suppress("UnstableApiUsage")

pluginManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    gradlePluginPortal()
  }

  includeBuild("../../../plugin")
}


dependencyResolutionManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    gradlePluginPortal()
  }
  versionCatalogs {
    create("libs") {
      from(files("../../../gradle/libs.versions.toml"))
    }
  }
}


plugins {
  id("org.danbrough.xtras.settings")
}

//includeBuild("../../../plugin")

rootProject.name = "plugin"