@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package org.danbrough.xtras

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

abstract class XtrasExtension(val project: Project) {

  @XtrasDSL
  var javaVersion = JavaVersion.VERSION_11

  @XtrasDSL
  var jvmTarget = JvmTarget.JVM_11

  @XtrasDSL
  var kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  var kotlinApiVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  abstract val nativeTargets: ListProperty<KonanTarget>

  private var environment: XtrasEnvironmentConfig = INITIAL_ENVIRONMENT

  @XtrasDSL
  abstract val libraries: ListProperty<XtrasLibrary>

  @XtrasDSL
  abstract val ldLibraryPath:Property<String>

  fun loadEnvironment(env: XtrasEnvironment, target: KonanTarget?): XtrasEnvironment {
    environment(env, target)
    return env
  }

  @XtrasDSL
  fun environment(block: XtrasEnvironmentConfig) {
    environment.also { oldEnvironment ->
      environment = { target ->
        oldEnvironment(target)
        block(target)
      }
    }
  }

  data class AndroidConfig(
    var ndkDir: File,
    var compileSDKVersion: Int = 34,
    var minSDKVersion: Int = 21,
    var ndkApiVersion: Int = minSDKVersion
  )

  val androidConfig = AndroidConfig(project.xtrasNdkDir)

  @XtrasDSL
  fun androidConfig(block: AndroidConfig.() -> Unit) {
    androidConfig.block()
  }

  @XtrasDSL
  var sh: File = project.projectProperty("xtras.sh") {
    if (HostManager.hostIsMingw)
      project.xtrasMsysDir.resolveAll("usr", "bin", "sh")
    else File("/bin/sh")
  }

}





