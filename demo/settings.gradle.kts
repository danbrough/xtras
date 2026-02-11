pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    //maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
  }
}


dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    maven("https://maven.danbrough.org")
    //maven("https://s01.oss.sonatype.org/content/groups/staging")
    google()
    mavenCentral()
  }
  /*  versionCatalogs {
      create("libs") {
        from(files("../gradle/libs.versions.toml"))
      }
    }*/
}




plugins {
  id("de.fayard.refreshVersions") version "0.60.6"
}

rootProject.name = "xtras_demo"



/*
dependencyResolutionManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    google()
    mavenCentral()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

*/
