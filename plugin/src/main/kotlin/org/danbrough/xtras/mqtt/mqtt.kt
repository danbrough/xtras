package org.danbrough.xtras.mqtt

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.registerGitLibrary
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.gradle.api.Project

const val MQTT_EXTN_NAME = "mqtt"
const val PROPERTY_MQTT_GROUP = "mqtt.group"
const val PROPERTY_MQTT_VERSION = "mqtt.version"
const val PROPERTY_MQTT_COMMIT = "mqtt.commit"
const val PROPERTY_MQTT_URL = "mqtt.url"

fun Project.mqtt(
	ssl: LibraryExtension,
	block: LibraryExtension.() -> Unit
) = registerGitLibrary<LibraryExtension>("mqtt") {
	dependsOn(ssl)

	configureSource { target ->
		val sourceDir = sourceDir(target)
		val installDir = buildDir(target)
		val compileDir = buildDir(target).resolve("build")
		val buildEnv = xtras.buildEnvironment

		workingDir(compileDir)
		outputs.file(compileDir.resolve("Makefile"))

		doFirst {
			if (compileDir.exists()) {
				compileDir.deleteRecursively()
			}
			compileDir.mkdirs()
			project.logInfo("Using cmake: ${buildEnv.binaries.cmake}")
		}

		val cmakeArgs = mutableListOf(
			buildEnv.binaries.cmake,
			"-G", "Unix Makefiles",
			"-DCMAKE_INSTALL_PREFIX=${installDir.absolutePath}",
			"-DPAHO_WITH_SSL=TRUE",
			"-DPAHO_BUILD_STATIC=TRUE",
			"-DPAHO_BUILD_SHARED=TRUE",
			"-DPAHO_ENABLE_TESTING=FALSE",
			"-DPAHO_BUILD_SAMPLES=TRUE",
			"-DPAHO_BUILD_DOCUMENTATION=FALSE",
			"-DOPENSSL_ROOT_DIR=${ssl.libsDir(target).absolutePath}",
		)

		cmakeArgs += sourceDir.absolutePath

		commandLine(cmakeArgs)
	}

	compileSource { target ->
		outputs.dir(buildDir(target))
		commandLine(xtras.buildEnvironment.binaries.make, "install")
	}

	block()
}

/*


@XtrasDSL
fun Project.mqtt(
  group: String = MQTT.group,
  name: String = MQTT.extensionName,
  version: String = projectProperty(PROPERTY_MQTT_VERSION, MQTT.extensionVersion),
  sourceCommit: String = projectProperty(PROPERTY_MQTT_COMMIT, MQTT.sourceCommit),
  sourceURL: String = MQTT.sourceURL,
  ssl: XtrasLibrary,
  configure: XtrasLibrary.() -> Unit = {}
) = registerLibrary(group, name, version, ssl) {
  gitSource(sourceURL, sourceCommit)

  cinterops {
    headers = """
          headers = MQTTAsync.h
          """.trimIndent()
  }


  configure()

  configureTargetTasks = { configureMqttTargetTasks(it) }
}


private fun XtrasLibrary.configureMqttTargetTasks(target: KonanTarget) {
  val compileDir = sourcesDir(target).resolve("build")
  val ssl = libraryDeps.first()
  val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
    dependsOn(*libraryDeps.map { it.extractArchiveTaskName(target) }.toTypedArray())

    doFirst {
      if (compileDir.exists()) {
        compileDir.deleteRecursively()
      }
      compileDir.mkdirs()
    }

    workingDir(compileDir)
    outputs.file(compileDir.resolve("Makefile"))

    val cmakeArgs = mutableListOf(
      buildEnvironment.binaries.cmake,
      "-G", "Unix Makefiles",
      "-DCMAKE_INSTALL_PREFIX=${buildDir(target).cygpath(buildEnvironment)}",
      "-DPAHO_WITH_SSL=TRUE",
      "-DPAHO_BUILD_STATIC=TRUE",
      "-DPAHO_BUILD_SHARED=TRUE",
      "-DPAHO_ENABLE_TESTING=FALSE",
      "-DPAHO_BUILD_SAMPLES=TRUE",
      "-DPAHO_BUILD_DOCUMENTATION=FALSE",
      "-DOPENSSL_ROOT_DIR=${ssl.libsDir(target).cygpath(buildEnvironment)}",
    )

    if (target.family == Family.ANDROID) {
      cmakeArgs += listOf(
        "-DANDROID_ABI=${target.androidLibDir}",
        "-DANDROID_PLATFORM=21",
        "-DCMAKE_TOOLCHAIN_FILE=${
          buildEnvironment.androidNdkDir.resolve("build/cmake/android.toolchain.cmake")
            .cygpath(buildEnvironment)
        }",
        "-DOPENSSL_SSL_LIBRARY=${
          ssl.libsDir(target).resolve("lib/libssl.so").cygpath(buildEnvironment)
        }",
        "-DOPENSSL_CRYPTO_LIBRARY=${
          ssl.libsDir(target).resolve("lib/libcrypto.so").cygpath(buildEnvironment)
        }",
        "-DOPENSSL_INCLUDE_DIR=${
          ssl.libsDir(target).resolve("include").cygpath(buildEnvironment)
        }",
        "-DOPENSSL_LIBRARIES=${
          ssl.libsDir(target).resolve("lib").cygpath(buildEnvironment)
        }",
      )
    } else if (target.family.isAppleFamily) {
      if (target == KonanTarget.MACOS_X64) cmakeArgs += "-DCMAKE_OSX_ARCHITECTURES=x86_64"
      else if (target == KonanTarget.MACOS_ARM64) cmakeArgs += "-DCMAKE_OSX_ARCHITECTURES=arm64"

    } else if (target.family == Family.MINGW) {
      cmakeArgs += listOf(
        "-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc",
        "-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++",
        "-DCMAKE_SYSTEM_NAME=Windows",
        "-DCMAKE_SYSTEM_VERSION=1",
        "-DOPENSSL_CRYPTO_LIBRARY=${
          ssl.libsDir(target).resolve("lib/libcrypto.a").cygpath(buildEnvironment)
        }",
        "-DOPENSSL_SSL_LIBRARY=${
          ssl.libsDir(target).resolve("lib/libssl.a").cygpath(buildEnvironment)
        }",
      )
    }
    cmakeArgs += ".."

    commandLine(cmakeArgs)
  }

  xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
    dependsOn(configureTask)
    workingDir(compileDir)
    commandLine(buildEnvironment.binaries.make, "install")
  }
}

 */