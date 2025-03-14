@file:Suppress("UnstableApiUsage")

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
  }
}

dependencyResolutionManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    google()
    mavenCentral()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "xtras"

//includeBuild("plugin")
includeBuild("plugin")


val pluginOnly: String? by settings


if (pluginOnly == null) {
  //includeBuild("core")
  /*includeBuild("libs/openssl/plugin") {
    name = "openssl_plugin"
  }*/

  includeBuild("libs/openssl/plugin") {
    name = "openssl_plugin"
  }

  //includeBuild("libs/ssh2/ssh2_plugin")

  listOf(
    //"support",
    //"jni",
    //"openssl",
    "openssl",
  ).forEach {
    include(":$it")
    project(":$it").projectDir = file("libs/$it")
  }

  include(":test2")
  
//  include(":test")

}



