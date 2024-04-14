package org.danbrough.xtras

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

object XtrasVersions {
  val javaVersion = JavaVersion.VERSION_1_8
  val jvmTarget = JvmTarget.JVM_1_8
  val kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0
  val kotlinApiVersion = KotlinVersion.DEFAULT
}

