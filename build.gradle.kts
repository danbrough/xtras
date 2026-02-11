@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.xtrasMavenDir


plugins {

  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.dokka)
  signing
  `maven-publish`
  alias(libs.plugins.xtras)
}

afterEvaluate {
  val libName = project.properties["libName"]
  val mavenDir = project.xtrasMavenDir


  val deleteMavenTask = tasks.register("deleteMavenTask") {
    doFirst {
      println("DELETEING MAVEN!!!!!!!!!!!!!!!!!")
      mavenDir.deleteRecursively()
    }
  }

  val rsyncMavenTask = tasks.register<Exec>("rsyncMavenTask") {
    doFirst {
      println("SYNCING MAVEN!!!!!!!!!!!!!!!!!")
    }
    dependsOn(deleteMavenTask)
    dependsOn(":$libName:publishAllPublicationsToXtrasRepository")
    workingDir(mavenDir)
    commandLine("rsync", "-avHSx", "./", "maven:~/m2/")
  }


  tasks.register("publishLib") {
    doFirst {
      println("PUBLISHING libName: $libName!!!!!!!!!!!!!!!!!!!!!!!!!")
      if (libName == null) error("property libName must be provided")
    }

    if (libName != null)
      dependsOn(rsyncMavenTask)
  }
}
