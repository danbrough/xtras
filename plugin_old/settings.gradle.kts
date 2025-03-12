pluginManagement {
  repositories {
    //maven("file:///files/xtras/maven")
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
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



