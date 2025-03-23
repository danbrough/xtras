package org.danbrough.xtras.tasks

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceConfigure
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xError
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xtrasName
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.Date

typealias ScriptEnvironment = MutableMap<String, Any>

abstract class ScriptTask : Exec() {

  init {
    group = Tasks.XTRAS_TASK_GROUP
  }

  @Input
  var target: KonanTarget = HostManager.host

  @Input
  lateinit var library: XtrasLibrary


  @InputFile
  val scriptFile = project.objects.fileProperty().convention {
    workingDir.resolve("xtras_${name}_${target.xtrasName}.sh")
  }

  private var scriptBlock: (PrintWriter.() -> Unit)? = null

  fun script(block: PrintWriter.() -> Unit) {
    scriptBlock = block
  }

  @InputFile
  val envFile = project.objects.fileProperty().convention {
    scriptFile.get().asFile.let {
      it.toPath().resolveSibling(it.absolutePath.replace(".sh", "_env.sh")).toFile()
    }
  }

  @TaskAction
  fun run() {

    val env = envFile.get().asFile
    project.xDebug("$name: writing $env")
    env.printWriter().use { writer ->
      writer.println("# generated ${Date()} by $name ${target.xtrasName}")
      writer.println("#")
      environment.forEach { (key, value) ->
        writer.println("$key=\"$value\"")
      }

    }

    val script = scriptFile.get().asFile
    project.xDebug("$name: writing $script")

    script.printWriter().use { writer ->
      writer.println("# generated ${Date()} by $name ${target.xtrasName}")
      writer.println("#")
      writer.println("source $env")
      writer.println()
      scriptBlock?.invoke(writer)
    }

    commandLine("sh", script)
    project.xDebug("$name: running ${commandLine.joinToString(" ")}")
  }


}

/*fun XtrasLibrary.configureSource2(config: Script.() -> Unit) {
  project.afterEvaluate {
    project.xInfo("configureSource(): targets: ${buildTargets.get().joinToString()}")

    buildTargets.get().forEach { target ->
      project.tasks.register<ScriptTask<XtrasLibrary>>(taskNameSourceConfigure(target)) {

        //dependsOn(this@configureSource2.taskNameSourceExtract(target))

        description = "Configure the source for ${this@configureSource2.name}"
        workingDir = sourcesDirMap(target)

        val scriptFile = scriptFileMap(Tasks.ACTION_CONFIGURE, target)

        val envFile =
          scriptFile.toPath().resolveSibling(scriptFile.name.replace(".sh", ".env")).toFile()

        xDebug("writing script file: ${scriptFile.absolutePath} envFile: ${envFile.absolutePath}")

        val script = Script(this@configureSource2, this@register, target)

        script.config()

        doFirst {
          envFile.printWriter().use { envWriter ->
            script.environment.forEach { (key, value) ->
              envWriter.println("$key=\"$value\"")
            }
          }
          if (!scriptFile.parentFile.exists()) scriptFile.parentFile.mkdirs()
          val scriptWriter = scriptFile.printWriter()

          scriptWriter.use {
            script.action?.invoke(this@register)
          }
          xInfo("configureSource(): running ${commandLine.joinToString(" ")}")
        }

        standardOutput = ByteArrayOutputStream()
        errorOutput = ByteArrayOutputStream()
        commandLine(xtras.binaries.sh.get(), scriptFile)

        doLast {
          val err = state.failure
          standardOutput.toString().trim().also {
            if (it != "") xInfo("$name: STDOUT: $standardOutput")
          }

          if (err != null) {
            xError("$name: STDERR: $errorOutput $err")
          } else errorOutput.toString().trim().also {
            if (it != "") xDebug("STDERR: $it")
          }
        }
      }
    }
  }
}*/


fun XtrasLibrary.configureSource(config: Script.() -> Unit) {
  project.afterEvaluate {
    project.xInfo("configureSource(): targets: ${buildTargets.get().joinToString()}")

    buildTargets.get().forEach { target ->
      project.tasks.register<ScriptTask>(taskNameSourceConfigure(target)) {

        dependsOn(this@configureSource.taskNameSourceExtract(target))

        description = "Configure the source for ${this@configureSource.name}"
        workingDir = sourcesDirMap(target)

        val scriptFile = scriptFileMap(Tasks.ACTION_CONFIGURE, target)

        val envFile =
          scriptFile.toPath().resolveSibling(scriptFile.name.replace(".sh", ".env")).toFile()

        xDebug("writing script file: ${scriptFile.absolutePath} envFile: ${envFile.absolutePath}")

        val script = Script(this@configureSource, this@register, target)

        script.config()

        doFirst {
          envFile.printWriter().use { envWriter ->
            script.environment.forEach { (key, value) ->
              envWriter.println("$key=\"$value\"")
            }
          }
          if (!scriptFile.parentFile.exists()) scriptFile.parentFile.mkdirs()
          val scriptWriter = scriptFile.printWriter()

          scriptWriter.use {
            script.action?.invoke(this@register)
          }
          xInfo("configureSource(): running ${commandLine.joinToString(" ")}")
        }

        standardOutput = ByteArrayOutputStream()
        errorOutput = ByteArrayOutputStream()
        commandLine(xtras.binaries.sh.get(), scriptFile)

        doLast {
          val err = state.failure
          standardOutput.toString().trim().also {
            if (it != "") xInfo("$name: STDOUT: $standardOutput")
          }

          if (err != null) {
            xError("$name: STDERR: $errorOutput $err")
          } else errorOutput.toString().trim().also {
            if (it != "") xDebug("STDERR: $it")
          }
        }
      }
    }
  }
}

