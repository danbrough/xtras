package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.xInfo
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.PrintWriter

typealias CInteropsTargetWriter = CInteropsConfig .(XtrasLibrary, KonanTarget, PrintWriter) -> Unit

val defaultCInteropsTargetWriter: CInteropsTargetWriter = { library, target, output ->
  val (libDir, includeDir) = library.libDirMap(target)
    .let { it.resolve("lib") to it.resolve("include") }
  output.println(
    """
      compilerOpts.${target.name} = -I$includeDir 
      linkerOpts.${target.name} = -L$libDir
      libraryPaths.${target.name} = $libDir 
    """.trimIndent()
  )
}

class CInteropsConfig(val library: XtrasLibrary) {
  val project = library.project

  val packageName: Property<String> =
    project.objects.property<String>().convention(project.provider {
      "${library.group.get()}.cinterops"
    })

  ///val enabled: Property<Boolean> = project.objects.property<Boolean>().convention(true)

  val codeFile = project.objects.fileProperty()

  internal var declaration: (PrintWriter.() -> Unit)? = null

  @XtrasDSL
  fun declaration(block: PrintWriter.() -> Unit) {
    declaration = block
  }

  internal var extraCode: (PrintWriter.() -> Unit)? = null

  @XtrasDSL
  fun extraCode(block: PrintWriter.() -> Unit) {
    extraCode = block
  }

  internal val targetWriter =
    project.objects.property<CInteropsTargetWriter>().convention(defaultCInteropsTargetWriter)

  @XtrasDSL
  fun targetWriter(block: CInteropsTargetWriter) {
    targetWriter.set(block)
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
        defFile.printWriter().use { writer ->
          writer.println("package = ${config.packageName.get()}")

          config.declaration?.invoke(writer)

          val targetWriter = config.targetWriter.get()
          buildTargets.get().forEach { target ->
            targetWriter(config, this@configureCinterops, target, writer)
          }

          if (config.codeFile.isPresent) {
            writer.println("---")
            writer.println(config.codeFile.get().asFile.readText())
          } else if (config.extraCode != null) {
            writer.println("---")
            config.extraCode?.invoke(writer)
          }
        }
      }
    }

    val kotlin = kotlinExtension as KotlinMultiplatformExtension

    val targets = buildTargets.get()


    val extractPackagesTaskName = TaskNames.create(
      TaskNames.GROUP_PACKAGE, TaskNames.ACTION_EXTRACT, this@configureCinterops.name
    )

    tasks.register(extractPackagesTaskName) {
      group = TaskNames.XTRAS_TASK_GROUP
      description = "Extract all the dependent binary packages"
      dependsOn(targets.map {
        TaskNames.create(
          TaskNames.GROUP_PACKAGE,
          TaskNames.ACTION_EXTRACT,
          libraryName = this@configureCinterops.name,
          it
        )
      })
    }

    kotlin.targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
      target.compilations["main"].cinterops.create(this@configureCinterops.name) {
        definitionFile.set(interopsFile)
        tasks[interopProcessingTaskName].dependsOn(
          extractPackagesTaskName, generateCinteropsTaskName
        )
      }
    }
  }
}

