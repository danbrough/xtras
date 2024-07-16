pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "xtras"

includeBuild("plugin")
//includeBuild("core")

val pluginOnly:String? by settings


if (pluginOnly == null) {
  listOf(
//  "logging",
 //   "support",
    "jni",
    //"openssl",

    //"ssh2",
    //"postgres",
    //"sqlite",
    //"jwt",
  ).forEach {
    include(":libs:$it")
  }
}




