package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

abstract class XtrasExtension(val project: Project) {
  abstract val message: Property<String>

  val buildEnvironment = project.xtrasBuildEnvironment()

  @XtraDSL
  abstract val nativeTargets: ListProperty<KonanTarget>

}
/*
fun Project.xtrasOld(block: (XtrasExtension.() -> Unit)? = null): XtrasExtension = xtrasVal.also {
  if (block != null) {
    extensions.configure(XtrasExtension::class.java, block)
  }
}


val Project.xtrasVal: XtrasExtension
  get() {
    pluginManager.findPlugin(XTRAS_PLUGIN_ID) ?: pluginManager.apply(XTRAS_PLUGIN_ID)
    return extensions.findByType<XtrasExtension>() ?: extensions.create(
      "xtrasExtension",
      XtrasExtension::class.java,
      this
    )
  }

fun XtrasExtension.declaredNativeTargets() =
  project.run {
    extensions.findByType<KotlinMultiplatformExtension>()?.run {
      nativeTargets = targets.withType<KotlinNativeTarget>().map { it.konanTarget }.toList()
      logTrace("xtras.nativeTargets = ${nativeTargets.joinToString { it.name }}")
      nativeTargets
    } ?: emptyList()
  }



*/


