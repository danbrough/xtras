pluginManagement {
  repositories {
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

rootProject.name = "core"
includeBuild("../plugin")

