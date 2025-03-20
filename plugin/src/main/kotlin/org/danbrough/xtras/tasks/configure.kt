package org.danbrough.xtras.tasks

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameSourceConfigure
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xError
import org.danbrough.xtras.xInfo
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter

class ConfigureScript(
  library: XtrasLibrary,
  val scriptWriter: PrintWriter,
  val task: Exec,
  val target: KonanTarget,
  val installDir: File
) : Script(library)

fun XtrasLibrary.configure(config: ConfigureScript.() -> Unit) {
  project.afterEvaluate {
    project.xInfo("configuring: targets: ${buildTargets.get().joinToString()}")
    buildTargets.get().forEach { target ->
      project.tasks.register<Exec>(taskNameSourceConfigure(target)) {
        dependsOn(this@configure.taskNameSourceExtract(target))
        group = Tasks.XTRAS_TASK_GROUP
        description = "Configure the source for ${this@configure.name}"
        workingDir = sourcesDirMap(target)
        val scriptFile = scriptFileMap(Tasks.ACTION_CONFIGURE, target)

        if (!scriptFile.parentFile.exists()) scriptFile.parentFile.mkdirs()
        val envFile =
          scriptFile.toPath().resolveSibling(scriptFile.name.replace(".sh", ".env")).toFile()

        xDebug("writing script file: ${scriptFile.absolutePath} envFile: ${envFile.absolutePath}")

        val scriptWriter = scriptFile.printWriter()
        val script =
          ConfigureScript(this@configure, scriptWriter, this@register, target, installDir(target))
        script.config()

        doFirst {
          scriptWriter.use {
            scriptWriter.println("source $envFile")
            scriptWriter.println()
          }

          envFile.printWriter().use { envWriter ->
            script.environment.forEach { (key, value) ->
              envWriter.println("$key=\"$value\"")
            }
          }
        }


        val stderr = ByteArrayOutputStream()
        val stdout = ByteArrayOutputStream()
        standardOutput = stdout
        errorOutput = stderr
        commandLine(xtras.binaries.sh.get(), scriptFile)
        doLast {
          xInfo("$name: STDOUT: $stdout")
          xError("$name: STDERR: $stderr")
        }
      }
    }
  }
}

