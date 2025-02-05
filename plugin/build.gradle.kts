import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.ir.backend.js.compile
import java.net.URI
import java.net.URL

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  alias(libs.plugins.dokka)
  signing
  id("org.danbrough.xtras") version "0.0.1-beta14"
}

group = "org.danbrough.xtras"

repositories {
  //maven("file:///files/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
  google()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  withSourcesJar()
//  withJavadocJar()
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_11
  }
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  //compileOnly(libs.dokka.gradle.plugin)
  compileOnly(libs.dokka.gradle.plugin)
  compileOnly(libs.gradle.android)
}

gradlePlugin {
  plugins {
    create("xtras") {
      id = "org.danbrough.xtras"
      implementationClass = "$group.XtrasPlugin"
      displayName = "Xtras Plugin"
      description = "Kotlin multiplatform support plugin"
    }
  }
}

dokka {
  pluginsConfiguration {
    html {
      footerMessage = "Dan Brough Org"
    }
  }
}

//dokka {
//  moduleName.set("Project Name")
//  dokkaSourceSets.main {
//    includes.from("README.md")
//    sourceLink {
//      localDirectory.set(file("src/main/kotlin"))
//      //remoteUrl.set(URI("https://example.com/src").toURL())
//      remoteLineSuffix.set("#L")
//    }
//  }
//  pluginsConfiguration.html {
//    customStyleSheets.from("styles.css")
//    customAssets.from("logo.png")
//    footerMessage.set("(c) Your Company")
//  }
//}

