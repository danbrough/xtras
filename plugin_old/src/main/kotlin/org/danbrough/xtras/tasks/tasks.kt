package org.danbrough.xtras.tasks

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.TaskConfig
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.platformName
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.io.Writer

fun LibraryExtension.registerTasks() {
  project.logInfo("registerTasks(): $this sourceConfig: $sourceConfig buildEnabled: $buildEnabled")


//  val buildRequiredGlobal: Boolean? =
//    project.projectProperty("${this@registerTasks.name}.buildRequired") { null }


//  buildRequired.convention {
//    buildRequiredGlobal ?: !packageFile(this).exists()
//  }

  supportedTargets.convention(xtras.nativeTargets)

//  sourcesRequired.convention(
//    supportedTargets.get().firstOrNull { target ->
//      buildRequired.get().invoke(target)
//    } != null
//  )


  registerSourceTasks()

  registerPackageTasks()

  registerCInteropsTasks()

  if (!buildEnabled) return

  fun TaskConfig.run() = supportedTargets.get().forEach {
    invoke(it)
  }

  taskPrepareSource?.run()

  taskConfigureSource?.run()

  taskCompileSource?.run()

  taskInstallSource?.run()
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


@XtrasDSL
fun LibraryExtension.sourceTask(
  name: SourceTaskName,
  dependsOn: SourceTaskName?,
  block: Exec.(KonanTarget) -> Unit
): TaskConfig = { target ->
  val taskName = xtrasTaskName(TASK_GROUP_SOURCE, name.name.lowercase(), this, target)
  project.tasks.register<Exec>(taskName) {
    dependsOn(*dependencies.map { it.taskNamePackageExtract(target) }.toTypedArray())
    group = XTRAS_TASK_GROUP
    environment(xtras.buildEnvironment.getEnvironment(target))
    onlyIf {
      forceBuild() || !packageFile(target).exists()
    }
    if (dependsOn != null)
      dependsOn(
        xtrasTaskName(
          TASK_GROUP_SOURCE,
          dependsOn.name.lowercase(),
          this@sourceTask,
          target
        )
      )

    workingDir(sourceDir(target))
    doFirst {
      project.logDebug("$name: running command: ${commandLine.joinToString(" ")}")
    }
    block(target)

    if (HostManager.hostIsMingw)
      commandLine(
        listOf(
          xtras.buildEnvironment.binaries.bash,
          "-cl",
          commandLine.joinToString(" ")
        )
      )

  }
}


@XtrasDSL
fun LibraryExtension.prepareSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskPrepareSource = sourceTask(SourceTaskName.PREPARE, dependsOn) {
    block(it)
  }
}


@XtrasDSL
fun LibraryExtension.configureSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskConfigureSource = sourceTask(SourceTaskName.CONFIGURE, dependsOn) {
    block(it)
  }
}


@XtrasDSL
fun LibraryExtension.compileSource(
  dependsOn: SourceTaskName? = SourceTaskName.CONFIGURE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskCompileSource = sourceTask(SourceTaskName.COMPILE, dependsOn) { target ->
    block(target)
  }
}


@XtrasDSL
fun LibraryExtension.installSource(
  dependsOn: SourceTaskName? = SourceTaskName.COMPILE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskInstallSource = sourceTask(SourceTaskName.INSTALL, dependsOn) {
    block(it)
  }
}

fun LibraryExtension.forceBuild(): Boolean = project.hasProperty("$name.forceBuild")