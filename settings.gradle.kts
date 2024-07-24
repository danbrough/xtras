pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "xtras"

includeBuild("plugin")

val pluginOnly:String? by settings


if (pluginOnly == null) {
  includeBuild("core")

  listOf(
//  "logging",
    "support",
    "jni",
    //"duckdb",
    "openssl",
    "duckdb",

    //"ssh2",
    //"postgres",
    //"sqlite",
    //"jwt",
  ).forEach {
    include(":$it")
    project(":$it").projectDir = file("libs/$it")
  }
}




