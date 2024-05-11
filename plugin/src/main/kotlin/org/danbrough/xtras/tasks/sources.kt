package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskConfig
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logTrace
import org.danbrough.xtras.resolveAll
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.io.Writer
import java.util.Date
import kotlin.math.log


@XtrasDSL
fun XtrasLibrary.sourceTask(
	name: SourceTaskName,
	dependsOn: SourceTaskName?,
	block: Exec.(KonanTarget) -> Unit
): TaskConfig = { target ->

	project.tasks.register<Exec>(name.taskName(this, target)) {
		group = XTRAS_TASK_GROUP
		enabled = buildEnabled

		dependsOn?.also {
			dependsOn(it.taskName(this@sourceTask, target))
		}

		onlyIf { !packageFile(target).exists() }

		workingDir(sourceDir(target))



		doFirst {
			environment(loadEnvironment(environment, target))
			project.logDebug("$name: running command: ${commandLine.joinToString(" ")}")
			project.logTrace("$name: environment: $environment")
			val scriptName = "${this@register.name}.sh"
			val scriptsDir = sourceDir(target).resolve("xtras")
			if (!scriptsDir.exists()) scriptsDir.mkdirs()
			scriptsDir.resolve(scriptName).printWriter().use { writer ->
				writer.println("# running ${this@register.name} at ${Date()}")
				writer.println()
				for (key in environment.keys)
					writer.println("${key}=${environment[key]}")
				writer.println()
				writer.println(commandLine.joinToString(" "))
			}
		}

		var logWriter: PrintWriter? = null
		processStdout {
			val writer =
				logWriter ?: workingDir.resolveAll("xtras", "${this@register.name}.log").printWriter()
					.also { logWriter = it }
			println(it)
			writer.println(it)
			writer.flush()
		}




		block(target)
	}
}
//  val taskName = ""//xtrasTaskName(TASK_GROUP_SOURCE, name.name.lowercase(), this, target)
//  project.tasks.register<Exec>(taskName) {
//    //TODO dependsOn(*dependencies.map { it.taskNamePackageExtract(target) }.toTypedArray())
//    group = XTRAS_TASK_GROUP
//    //TODO environment(xtras.buildEnvironment.getEnvironment(target))
////    onlyIf {
////      forceBuild() || !packageFile(target).exists()
////    }
////    if (dependsOn != null)
////      dependsOn(
////        xtrasTaskName(
////          TASK_GROUP_SOURCE,
////          dependsOn.name.lowercase(),
////          this@sourceTask,
////          target
////        )
////      )
//
//    //workingDir(sourceDir(target))
//    doFirst {
//      project.logDebug("$name: running command: ${commandLine.joinToString(" ")}")
//    }
//    block(target)


@XtrasDSL
fun XtrasLibrary.prepareSource(
	dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
	block: Exec.(KonanTarget) -> Unit
) {
	taskPrepareSource = sourceTask(SourceTaskName.PREPARE, dependsOn) {
		block(it)
	}
}


@XtrasDSL
fun XtrasLibrary.configureSource(
	dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
	block: Exec.(KonanTarget) -> Unit
) {
	taskConfigureSource = sourceTask(SourceTaskName.CONFIGURE, dependsOn) {
		block(it)
	}
}


@XtrasDSL
fun XtrasLibrary.compileSource(
	dependsOn: SourceTaskName? = SourceTaskName.CONFIGURE,
	block: Exec.(KonanTarget) -> Unit
) {
	taskCompileSource = sourceTask(SourceTaskName.COMPILE, dependsOn) { target ->
		val taskFile = workingDir.resolve(".xtras_compiled")
		outputs.file(taskFile)
		doLast {
			taskFile.createNewFile()
		}
		block(target)
	}
}


@XtrasDSL
fun XtrasLibrary.installSource(
	dependsOn: SourceTaskName? = SourceTaskName.COMPILE,
	block: Exec.(KonanTarget) -> Unit
) {

	taskInstallSource = sourceTask(SourceTaskName.INSTALL, dependsOn) { target ->
		outputs.dir(buildDir(target))
		doLast {
			workingDir.resolve("xtras")
				.copyRecursively(buildDir(target).resolve("xtras"), overwrite = true)
		}
		block(target)
	}
}

fun Exec.processStdout(
	processLine: (String) -> Unit = { println(it) }
) {
	doFirst {
		val pin = PipedInputStream()
		this@processStdout.standardOutput = PipedOutputStream(pin)
		Thread {
			InputStreamReader(pin).useLines { lines ->
				lines.forEach { line ->
					processLine(line)
				}
			}
		}.start()
	}
}