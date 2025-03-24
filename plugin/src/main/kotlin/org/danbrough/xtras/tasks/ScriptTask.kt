package org.danbrough.xtras.tasks

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xtrasName
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.PrintWriter
import java.util.Date

typealias ScriptEnvironment = MutableMap<String, Any>

fun scriptEnvironment(): ScriptEnvironment = mutableMapOf()


@Suppress("MemberVisibilityCanBePrivate")
abstract class ScriptTask : Exec() {

  init {
    group = Tasks.XTRAS_TASK_GROUP
  }

  @Input
  var target: KonanTarget = HostManager.host

  @OutputFile
  val scriptFile = project.objects.fileProperty().convention {
    workingDir.resolve("xtras_${name}_${target.xtrasName}.sh").also {
      if (!it.exists()) it.createNewFile()
    }
  }

  @OutputFile
  val envFile = project.objects.fileProperty().convention {
    scriptFile.get().asFile.let { script ->
      script.toPath().resolveSibling(script.absolutePath.replace(".sh", "_env.sh")).toFile().also {
        if (!it.exists()) it.createNewFile()
      }
    }
  }

  private var scriptBlock: (PrintWriter.() -> Unit)? = null

  fun script(block: PrintWriter.() -> Unit) {
    scriptBlock = block
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
      writer.println(". $env")
      writer.println()
      scriptBlock?.invoke(writer)
    }

    commandLine("sh", scriptFile.get().asFile)
    project.xDebug("$name: running ${commandLine.joinToString(" ")}")
  }


}
