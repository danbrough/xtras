package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.unixPath
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter
import java.util.Date

fun XtrasLibrary.registerBuildTask(target: KonanTarget) {

	val srcDir = sourceDir(target)

	fun xtrasDir(name: String): Lazy<File> = lazy {
		srcDir.resolveAll("xtras", name).also {
			if (!it.exists()) it.mkdirs()
		}
	}


	val buildTaskName = SourceTaskName.BUILD.taskName(this, target)
	val genEnvTaskName = "${buildTaskName}_env"

	val scriptsDir by xtrasDir("scripts")
	val logsDir by xtrasDir("logs")
	val logWriter by lazy {
		PrintWriter(logsDir.resolve("${buildTaskName}.log"))
	}

	val envFile by lazy {
		scriptsDir.resolve("env.sh")
	}

	val buildScript by lazy {
		scriptsDir.resolve("build.sh")
	}


	val genTask = project.tasks.register(genEnvTaskName) {
		doFirst {
			envFile.printWriter().use { writer ->
				loadEnvironment(target).also { env ->
					env.keys.forEach { key ->
						writer.println("$key=\"${env[key]}\"")
					}
				}

				writer.println("MESSAGE=\"Hello world\nat ${Date()}\"")
			}

			buildScript.printWriter().use { writer->
				writer.println("#!/bin/sh")
				writer.println()
				writer.println("source ${project.unixPath(envFile)}")
				writer.println("echo the message is \$MESSAGE")

			}
		}
		outputs.file(envFile)
	}

	project.tasks.register<Exec>(buildTaskName) {
		dependsOn(SourceTaskName.EXTRACT.taskName(this@registerBuildTask, target), genTask)

		workingDir = srcDir
		commandLine(
			xtras.sh,
			"-c",
			project.unixPath(buildScript)
		)

		doFirst {
			logWriter.println("# ${this@register.name}: running ${commandLine.joinToString(" ")}")
			logWriter.println()
		}

		processStdout {
			println(it)
			logWriter.println(it)
			logWriter.flush()
		}
	}
}
