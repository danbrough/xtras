package org.danbrough.xtras.tasks

import org.danbrough.xtras.CInteropsTargetWriter
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logError
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.logTrace
import org.danbrough.xtras.mixedPath
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess


internal fun XtrasLibrary.registerCInteropsTasks() {
  val config = cinteropsConfig ?: return

  project.logInfo("$name::registerCinteropsTasks()")
  if (!config.isStatic)
    registerGenerateCInterops()

  val kotlin = project.kotlinExtension
  if (kotlin !is KotlinMultiplatformExtension) return
  kotlin.targets.withType<KotlinNativeTarget> {
    compilations["main"].cinterops.create(this@registerCInteropsTasks.name) {
      defFile = config.defFile
      config.codeFile?.also {
        includeDirs(it.parentFile)
      }
    }
  }
}

val defaultCInteropsTargetWriter: CInteropsTargetWriter = { config, target, writer ->
  val libDir = libsDir(target).resolve("lib").mixedPath
  val includeDir = libsDir(target).resolve("include").mixedPath
  writer.println(
    """
         |compilerOpts.${target.name} =  -I$includeDir ${
      config.extraLibsDirs.joinToString {
        "-I${
          it(target).resolve(
            "include"
          ).mixedPath
        } "
      }
    }
         |linkerOpts.${target.name} = -L$libDir ${
      config.extraLibsDirs.joinToString {
        "-L${
          it(target).resolve(
            "lib"
          ).mixedPath
        } "
      }
    }
         |libraryPaths.${target.name} =  $libDir ${
      config.extraLibsDirs.joinToString {
        "${
          it(target).resolve(
            "lib"
          ).mixedPath
        } "
      }
    }
         |""".trimMargin()
  )
}

private fun XtrasLibrary.registerGenerateCInterops() {
  val config = cinteropsConfig!!
  project.logInfo("$name::registerGenerateCInterops()")

  val cinteropsGenerateTaskName = InteropsTaskName.GENERATE.taskName(this)
  project.tasks.register(cinteropsGenerateTaskName) {
    group = XTRAS_TASK_GROUP
    description = "Generates the interops def file at ${config.defFile.mixedPath}"

    config.codeFile?.also {
      inputs.dir(it.parentFile)
    }

    inputs.property("config", config.hashCode().toString())

    outputs.file(config.defFile)

    doFirst {
      config.defFile.parentFile.also {
        if (!it.exists()) it.mkdirs()
      }
    }

    actions.add {
      config.defFile.printWriter().use { writer ->
        writer.println("package = ${config.interopsPackage}")

        config.declaration?.also {
          writer.println(it)
        }

        //xtras.nativeTargets.get().filter(config.targetWriterFilter).forEach {
        xtras.nativeTargets.get().forEach {
          config.targetWriter(this@registerGenerateCInterops, config, it, writer)
        }

        config.code?.also {
          writer.println("---")
          writer.println(it)
        }

        config.codeFile?.also {
          if (config.code == null) writer.println("---")
          writer.println(it.readText())
        }
      }
    }

    doLast {
      project.logDebug("$name: wrote ${config.defFile.absolutePath} ")
    }
  }

  project.tasks.withType<CInteropProcess> {
    dependsOn(cinteropsGenerateTaskName)
    dependsOn(PackageTaskName.EXTRACT.taskName(this@registerGenerateCInterops, konanTarget))
    inputs.file(config.defFile)
  }
}

