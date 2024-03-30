package org.danbrough.xtras.tasks

import org.danbrough.xtras.CInteropsTargetWriter
import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.envLibraryPathName
import org.danbrough.xtras.logError
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.targetNameMap
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.environment
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.PrintWriter


val defaultCInteropsTargetWriter: CInteropsTargetWriter = { target, writer ->
  val libDir = libsDir(target).absolutePath
  writer.println(
    """
         |compilerOpts.${target.name} =  -I$libDir/include
         |linkerOpts.${target.name} = -L$libDir/lib
         |libraryPaths.${target.name} =  $libDir/lib
         |""".trimMargin()
  )
}

internal fun LibraryExtension.taskNameCInterops(): String =
  xtrasTaskName("cinterops", "create", this)

internal fun LibraryExtension.writeInteropsFile(printerWriter: PrintWriter) {
  printerWriter.use { writer ->

    (cinteropsConfig.headers ?: cinteropsConfig.headersFile?.readText())?.also {
      writer.println(it)
    }

    supportedTargets.get().forEachIndexed { index, konanTarget ->
      if (index == 0) writer.println("\n### XTRAS: generated paths from the cinteropsTargetWriter\n")
      cinteropsConfig.cinteropsTargetWriter.invoke(this, konanTarget, writer)
    }

    writer.println("---")
    writer.println()

    val interops = "/${group.replace('.', '/')}/interops.h"

    this::class.java.getResourceAsStream(interops)?.also {
      writer.println()
      writer.println("// interops header code from resource: $interops")
      writer.println(it.readAllBytes().decodeToString())
    }

    (cinteropsConfig.code ?: cinteropsConfig.codeFile?.readText())?.also {
      writer.println(it)
    }
  }
}


private fun LibraryExtension.registerCInteropsTask() =
  project.tasks.register(taskNameCInterops()) {
    group = XTRAS_TASK_GROUP
    val interopsFile = cinteropsConfig.defFile
    inputs.property("cinterops", cinteropsConfig.hashCode())

    cinteropsConfig.headersFile?.also {
      inputs.file(it)
    }
    cinteropsConfig.codeFile?.also {
      inputs.file(it)
    }
    outputs.file(interopsFile)

    doFirst {
      project.logInfo("creating cinterops file for $name at ${interopsFile.absolutePath}")
      if (cinteropsConfig.headers != null && cinteropsConfig.headersFile != null)
        error("Only one of cinterops.headers or cinterops.headersFile should be provided")
      if (cinteropsConfig.code != null && cinteropsConfig.codeFile != null)
        error("Only one of cinterops.code or cinterops.codeFile should be provided")
    }

    actions.add {
      interopsFile.printWriter().use {
        writeInteropsFile(it)
      }
    }

    doLast {
      project.logWarn("$name didWork: $didWork")
    }
  }


fun LibraryExtension.registerCInteropsTasks() {
  val interopsTask: TaskProvider<*> = registerCInteropsTask()

  project.tasks.withType<CInteropProcess> {
    dependsOn(interopsTask)
  }

  (project.kotlinExtension as KotlinMultiplatformExtension).targets.withType<KotlinNativeTarget> {
    compilations["main"].cinterops.create(this@registerCInteropsTasks.name) {
      defFile = cinteropsConfig.defFile
    }

    binaries.all {
      if (this is Executable) {
        runTask?.apply {
          val libPath = buildString {
            append(libsDir(konanTarget).resolve("lib").absolutePath)
            if (environment.containsKey(konanTarget.envLibraryPathName)) {
              append("${File.pathSeparator}${environment[konanTarget.envLibraryPathName]}")
            }
          }
          environment[konanTarget.envLibraryPathName] = libPath
        }
      }
    }
  }

  project.tasks.withType<CInteropProcess> {
    dependsOn(taskNameCInterops(), taskNamePackageExtract(konanTarget))
    //dependsOn(taskNamePackageExtract(konanTarget))
  }

  project.tasks.withType<KotlinNativeHostTest> {
    val konanTarget = KonanTarget.targetNameMap[targetName] ?: error("Failed to find konanTarget for $targetName")

    val libPath = buildString {
      append(libsDir(konanTarget).resolve("lib").absolutePath)
      if (environment.containsKey(konanTarget.envLibraryPathName)) {
        append("${File.pathSeparator}${environment[konanTarget.envLibraryPathName]}")
      }
    }
    environment(konanTarget.envLibraryPathName, libPath)
  }


}