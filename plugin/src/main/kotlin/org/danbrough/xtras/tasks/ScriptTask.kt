package org.danbrough.xtras.tasks

import org.danbrough.xtras.Tasks
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xtrasName
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
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
  val target = project.objects.property<KonanTarget>()

  @OutputFile
  val scriptFile = project.objects.fileProperty().convention {
    workingDir.resolve("xtras_${name}_${target.get().xtrasName}.sh").also {
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
      writer.println("# generated ${Date()} by $name ${target.get().xtrasName}")
      writer.println("#")
      environment.forEach { (key, value) ->
        writer.println("export $key=\"$value\"")
      }
    }

    val script = scriptFile.get().asFile
    project.xDebug("$name: writing $script")

    script.printWriter().use { writer ->
      writer.println("#!${project.xtras.binaries.bash.get()}")
      writer.println("# generated ${Date()} by $name ${target.get().xtrasName}")
      writer.println("#")
      writer.println("""cd "${'$'}(dirname "${'$'}0")"""")
      writer.println(". $env")
      writer.println()
      scriptBlock?.invoke(writer)
    }

    commandLine(project.xtras.binaries.bash.get(), scriptFile.get().asFile)
    project.xDebug("$name: running ${commandLine.joinToString(" ")}")
  }

  fun clearEnvironment(): ScriptEnvironment = environment.apply { clear() }

  fun defaultEnvironment(): ScriptEnvironment = environment.apply {
    put("PATH", project.xtras.environment.pathDefault.get())
  }

}
