@file:Suppress("UnstableApiUsage")

import org.danbrough.xtras.xWarn
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
  val libName = project.properties["lib"]
  
  if (libName != null) {
    val mavenDir = project.xtrasMavenDir

    val deleteMavenTask = tasks.register("deleteMavenTask") {
      doFirst {
        xWarn("deleting $mavenDir!")
        mavenDir.deleteRecursively()
      }
    }

    tasks.register<Exec>("publishLib") {
      dependsOn(deleteMavenTask)
      dependsOn(":$libName:publishAllPublicationsToXtrasRepository")
      workingDir(mavenDir)
      commandLine("rsync", "-avHSx", "./", "maven:~/m2/")
    }
  }
}
