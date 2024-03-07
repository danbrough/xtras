pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    mavenCentral()
    gradlePluginPortal()
  }
}


dependencyResolutionManagement {
  versionCatalogs {
    create("xtras") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}


//includeBuild("../support")

