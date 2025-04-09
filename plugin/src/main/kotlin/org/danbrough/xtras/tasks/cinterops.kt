package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xtrasName
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.PrintWriter

class CInteropsConfig(private val library: XtrasLibrary) {
  private val project = library.project

  val packageName: Property<String> =
    project.objects.property<String>().convention(project.provider {
      "${library.group.get()}.cinterops"
    })

  val enabled: Property<Boolean> = project.objects.property<Boolean>().convention(true)

  internal var declaration: (PrintWriter.() -> Unit)? = null

  @XtrasDSL
  fun declaration(block: PrintWriter.() -> Unit) {
    declaration = block
  }
}

@XtrasDSL
fun XtrasLibrary.cinterops(action: Action<CInteropsConfig>) {
  if (cinterops.isPresent) action.invoke(cinterops.get()) else CInteropsConfig(this).also {
    cinterops.set(it)
    action.invoke(it)
    configureCinterops(it)
  }
}


private fun XtrasLibrary.configureCinterops(config: CInteropsConfig) {
  project.afterEvaluate {
    xInfo("$name: configureCInterops(): config:$config")
    val generateCinteropsTaskName =
      TaskNames.create("generate", "cinterops", this@configureCinterops.name)

    tasks.register(generateCinteropsTaskName) {
      outputs.file(interopsFile)
      group = TaskNames.XTRAS_TASK_GROUP

      doFirst {
        val defFile = interopsFile.get().asFile
        xInfo("$name: generating cinterops file $defFile ..")
        defFile.printWriter().use {
          it.println("package = ${config.packageName.get()}")
          config.declaration?.invoke(it)
        }
      }
    }

    val kotlin = kotlinExtension as KotlinMultiplatformExtension
    kotlin.targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
      target.compilations["main"].cinterops.create("xtras${this@configureCinterops.name.capitalized()}${target.konanTarget.xtrasName}") {
        tasks[interopProcessingTaskName].dependsOn(generateCinteropsTaskName)
        definitionFile.set(interopsFile)
      }
    }
  }
}

