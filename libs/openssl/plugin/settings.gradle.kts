

pluginManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
    gradlePluginPortal()
  }
/*

  plugins{
    id("org.danbrough.xtras.settings")
  }
*/

}

includeBuild("../../../plugin")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../../../gradle/libs.versions.toml"))
    }
  }
}


rootProject.name = "plugin"