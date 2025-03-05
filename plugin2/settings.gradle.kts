pluginManagement {
  repositories {
    //maven("file:///files/xtras/maven")
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    gradlePluginPortal()
  }
}


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


