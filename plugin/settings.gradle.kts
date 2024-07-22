pluginManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    //maven("https://maven.danbrough.org")

    mavenCentral()
    gradlePluginPortal()
  }
}


dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}



