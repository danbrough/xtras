package org.danbrough.xtras

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.KonanTarget

abstract class XtrasExtension(val project: Project) {

  @XtrasDSL
  var message: String = "default message for project: ${project.name}"

  @XtrasDSL
  var javaVersion = JavaVersion.VERSION_17

  @XtrasDSL
  var jvmTarget = JvmTarget.JVM_17

  @XtrasDSL
  var kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  var kotlinApiVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  abstract val nativeTargets: ListProperty<KonanTarget>

}



