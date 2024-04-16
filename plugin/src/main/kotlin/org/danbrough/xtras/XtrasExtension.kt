@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package org.danbrough.xtras

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

abstract class XtrasExtension(val project: Project) {

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

  @XtrasDSL
  val tools = Tools(ToolsDelegate(project))

  @XtrasDSL
  fun tools(block: Tools.() -> Unit) {
    tools.block()
  }

  private var environment: XtrasEnvironmentConfig = XTRAS_DEFAULT_ENVIRONMENT

  @XtrasDSL
  var cleanEnvironment: Boolean = false

  fun loadEnvironment(env: XtrasEnvironment, target: KonanTarget? = null): XtrasEnvironment {
    if (cleanEnvironment) env.clear()
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
  )

  val androidConfig = AndroidConfig(project.xtrasNdkDir)

  @XtrasDSL
  fun androidConfig(block: AndroidConfig.() -> Unit) {
    androidConfig.block()
  }

}





