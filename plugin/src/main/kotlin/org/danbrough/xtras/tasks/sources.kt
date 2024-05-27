package org.danbrough.xtras.tasks

import org.gradle.api.tasks.Exec
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream


/*
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

		onlyIf { !packageFile(target).exists() || project.hasProperty("forceBuild") }

		workingDir(sourceDir(target))

		environment(loadEnvironment(target))

		doFirst {

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

		val logWriter: PrintWriter by lazy {
			workingDir.resolveAll("xtras", "${this@register.name}.log").printWriter()
		}

		processStdout {
			println(it)
			logWriter.println(it)
			logWriter.flush()
		}

		processStderr {
			println(it)
			logWriter.println(it)
			logWriter.flush()
		}

		block(target)
	}
}
*/



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

fun Exec.processStderr(
	processLine: (String) -> Unit = { println(it) }
) {
	doFirst {
		val pin = PipedInputStream()
		this@processStderr.errorOutput = PipedOutputStream(pin)
		Thread {
			InputStreamReader(pin).useLines { lines ->
				lines.forEach { line ->
					processLine(line)
				}
			}
		}.start()
	}
}