package org.danbrough.xtras.tasks

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.TaskConfig
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtraDSL
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.platformName
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.io.Writer

fun LibraryExtension.registerTasks() {
  project.logInfo("registerTasks(): $this sourceConfig: $sourceConfig")

  buildRequired.convention {
    !packageFile(this).exists()
  }

  supportedTargets.convention(xtras.nativeTargets)

  sourcesRequired.convention(
    supportedTargets.get().firstOrNull { target ->
      buildRequired.get().invoke(target)
    } != null
  )


  registerSourceTasks()

  fun TaskConfig.run() = supportedTargets.get().forEach {
    invoke(it)
  }

  taskPrepareSource?.run()

  taskConfigureSource?.run()

  taskCompileSource?.run()

  registerPackageTasks()


  registerCInteropsTasks()
}


fun xtrasTaskName(
  group: String,
  name: String,
  libraryExtension: LibraryExtension? = null,
  target: KonanTarget? = null
): String =
  "xtras${group.capitalized()}${name.capitalized()}${libraryExtension?.name?.capitalized() ?: ""}${target?.platformName?.capitalized() ?: ""}"

const val TASK_GROUP_SOURCE = "source"
const val TASK_GROUP_PACKAGE = "package"

enum class SourceTaskName {
  DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL
}

enum class PackageTaskName {
  CREATE, EXTRACT, DOWNLOAD, PROVIDE
}

fun LibraryExtension.taskNameDownloadSource(): String =
  xtrasTaskName(TASK_GROUP_SOURCE, SourceTaskName.DOWNLOAD.name.lowercase(), this)

fun LibraryExtension.taskNameExtractSource(target: KonanTarget): String =
  xtrasTaskName(TASK_GROUP_SOURCE, SourceTaskName.EXTRACT.name.lowercase(), this, target)

fun LibraryExtension.taskNamePackageCreate(target: KonanTarget): String =
  xtrasTaskName(TASK_GROUP_PACKAGE, PackageTaskName.CREATE.name.lowercase(), this, target)

fun LibraryExtension.taskNamePackageExtract(target: KonanTarget): String =
  xtrasTaskName(TASK_GROUP_PACKAGE, PackageTaskName.EXTRACT.name.lowercase(), this, target)

fun LibraryExtension.taskNamePackageDownload(target: KonanTarget): String =
  xtrasTaskName(TASK_GROUP_PACKAGE, PackageTaskName.DOWNLOAD.name.lowercase(), this, target)

fun LibraryExtension.taskNamePackageProvide(target: KonanTarget): String =
  xtrasTaskName(TASK_GROUP_PACKAGE, PackageTaskName.PROVIDE.name.lowercase(), this, target)

fun Exec.processStdout(
  output: Writer? = null,
  processLine: (String) -> Unit = { println(it) }
) {
  doFirst {
    val pin = PipedInputStream()
    this@processStdout.standardOutput = PipedOutputStream(pin)
    Thread {
      val printWriter = output?.let { PrintWriter(it) }
      InputStreamReader(pin).useLines { lines ->
        lines.forEach { line ->
          printWriter?.also {
            it.println(line)
            it.flush()
          }
          processLine(line)
        }
      }
    }.start()
  }
}


@XtraDSL
fun LibraryExtension.sourceTask(
  name: SourceTaskName,
  dependsOn: SourceTaskName?,
  block: Exec.(KonanTarget) -> Unit
): TaskConfig = { target ->
  val taskName = xtrasTaskName(TASK_GROUP_SOURCE, name.name.lowercase(), this, target)
  project.tasks.register<Exec>(taskName) {
    group = XTRAS_TASK_GROUP
    environment(xtras.buildEnvironment.getEnvironment(target))
    if (dependsOn != null)
      dependsOn(
        xtrasTaskName(
          TASK_GROUP_SOURCE,
          dependsOn.name.lowercase(),
          this@sourceTask,
          target
        )
      )
    onlyIf {
      buildRequired.get().invoke(target)
    }
    workingDir(sourceDir(target))
    doFirst {
      project.logDebug("$name: running command: ${commandLine.joinToString(" ")}")
    }
    block(target)
  }
}


@XtraDSL
fun LibraryExtension.prepareSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskPrepareSource = sourceTask(SourceTaskName.PREPARE, dependsOn) {
    block(it)
  }
}


@XtraDSL
fun LibraryExtension.configureSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskConfigureSource = sourceTask(SourceTaskName.CONFIGURE, dependsOn) {
    block(it)
  }
}


@XtraDSL
fun LibraryExtension.compileSource(
  dependsOn: SourceTaskName? = SourceTaskName.CONFIGURE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskCompileSource = sourceTask(SourceTaskName.COMPILE, dependsOn) {
    block(it)
  }
}




