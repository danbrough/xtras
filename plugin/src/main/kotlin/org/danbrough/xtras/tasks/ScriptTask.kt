package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskNames
import org.danbrough.xtras.Xtras.Companion.xtras
import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xtrasName
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter
import java.util.Date

typealias ScriptEnvironment = MutableMap<String, Any>


@Suppress("MemberVisibilityCanBePrivate")
abstract class ScriptTask : Exec() {

  init {
    group = TaskNames.XTRAS_TASK_GROUP
  }

  @Input
  val target: Property<KonanTarget> =
    project.objects.property<KonanTarget>()

  @OutputDirectory
  @Optional
  val outputDirectory: Property<File> =
    project.objects.property<File>()

  @OutputFile
  @Optional
  val outputFile = project.objects.property<File>()


  @OutputFile
  val scriptFile: RegularFileProperty = project.objects.fileProperty().convention {
    File(workingDir, ("xtras_${name}_${target.get().xtrasName}.sh"))
  }

  @OutputFile
  val envFile: RegularFileProperty = project.objects.fileProperty().convention {
    scriptFile.get().asFile.let { script ->
      script.toPath().resolveSibling(script.absolutePath.replace(".sh", "_env.sh")).toFile()
    }
  }

  private var scriptBlock: (PrintWriter.() -> Unit)? = null

  fun script(block: PrintWriter.() -> Unit) {
    scriptBlock = block
  }


  /*  private var envBlock: (PrintWriter.() -> Unit)? = null

    fun env(block: PrintWriter.() -> Unit) {
      envBlock = block
    }*/

  private val bash = project.xtras.binaries.bash.get()

  @TaskAction
  fun run() {

    val env = envFile.get().asFile
    xDebug("$name: writing $env")

    env.printWriter().use { writer ->
      writer.println("# generated ${Date()} by $name ${target.get().xtrasName}")
      writer.println("#")
      environment.forEach { (key, value) ->
        writer.println("export $key=\"$value\"")
      }
    }

    val script = scriptFile.get().asFile
    xDebug("$name: writing $script")

    script.printWriter().use { writer ->
      writer.println("#!$bash")
      writer.println("# generated ${Date()} by $name ${target.get().xtrasName}")
      writer.println("#")
      writer.println($$"""cd "$(dirname "$0")"""")
      writer.println(". $env")
      writer.println()
      scriptBlock?.invoke(writer)
    }

    commandLine(bash, scriptFile.asFile.get())
    xDebug("$name: running ${commandLine.joinToString(" ")}")
  }

  fun clearEnvironment(): ScriptEnvironment = environment.apply { clear() }

  private val pathDefault = project.xtras.environment.pathDefault.get()

  @Suppress("SpellCheckingInspection")
  fun defaultEnvironment(): ScriptEnvironment = environment.apply {
    put("PATH", pathDefault)
    put("MAKEFLAGS", "-j${Runtime.getRuntime().availableProcessors()}")
    put("MAKEOPTS", "-j${Runtime.getRuntime().availableProcessors()}")
  }

}
