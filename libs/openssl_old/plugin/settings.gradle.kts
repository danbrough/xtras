pluginManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../../../gradle/libs.versions.toml"))
    }
  }
}


rootProject.name = "plugin"