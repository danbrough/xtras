package org.danbrough.xtras

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.KonanTarget

abstract class XtrasExtension(val project: Project) {

  @XtrasDSL
  var javaVersion = JavaVersion.VERSION_1_8

  @XtrasDSL
  val jvmTarget = JvmTarget.JVM_1_8

  @XtrasDSL
  var kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  var kotlinApiVersion = KotlinVersion.DEFAULT

  @XtrasDSL
  abstract val nativeTargets: ListProperty<KonanTarget>

}



