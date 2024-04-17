@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.konanDir

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.xtras) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  `maven-publish`
}


val xtrasProjectGroup = XTRAS_PACKAGE
val xtrasProjectVersion: String = libs.versions.xtras.version.get()


allprojects {
  group = xtrasProjectGroup
  version = xtrasProjectVersion

  repositories {
    maven("https://maven.danbrough.org")
    //maven(xtrasPath(XtrasPath.MAVEN))
    mavenCentral()
    google()
    //   maven("https://s01.oss.sonatype.org/content/groups/staging")
  }
}


tasks.register<Exec>("thang") {
  doFirst {
    environment.clear()
    environment("PATH", "/bin:/usr/bin:/usr/local/bin")
    val home = System.getProperty("user.home")
    println("HOME is $home")
    println("running thang with environment: $environment")
    val depsDir = konanDir.resolve("dependencies")
    println("konan deps dir: $depsDir")
    depsDir.listFiles()?.first {
      it.isDirectory && it.name.startsWith("llvm-")
    }?.also {
      println("FOUND LLVM: $it")
    }
  }
  commandLine("sh", "-c", "echo \"the date is `date`\"")
}