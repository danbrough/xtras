package org.danbrough.xtras.mqtt

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.androidLibDir
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.registerGitLibrary
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


fun Project.mqtt(
  ssl: LibraryExtension,
  block: LibraryExtension.() -> Unit = {}
) = registerGitLibrary<LibraryExtension>("mqtt") {
  dependsOn(ssl)

  configureSource { target ->
    val installDir = buildDir(target)
    val buildEnv = xtras.buildEnvironment

    environment("CFLAGS", "${environment["CFLAGS"]?.toString() ?: ""} -Wno-deprecated-declarations")
    doFirst {
      project.logDebug("CFLAGS are: ${environment["CFLAGS"]}")
    }

    outputs.file(workingDir.resolve("Makefile"))

    doFirst {
      project.logInfo("Using cmake: ${buildEnv.binaries.cmake}")
    }

    val sslDir = ssl.libsDir(target)

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
      "-DOPENSSL_ROOT_DIR=${sslDir.absolutePath}",
    )

    if (target.family == Family.ANDROID) {
      cmakeArgs += listOf(
        "-DANDROID_ABI=${target.androidLibDir}",
        "-DANDROID_PLATFORM=21",
        "-DCMAKE_TOOLCHAIN_FILE=${buildEnv.androidNdkDir.resolve("build/cmake/android.toolchain.cmake")}",
        "-DOPENSSL_INCLUDE_DIR=${sslDir.resolve("include")}",
        "-DOPENSSL_CRYPTO_LIBRARY=${sslDir.resolve("lib/libcrypto.so")}",
        "-DOPENSSL_SSL_LIBRARY=${sslDir.resolve("lib/libssl.so")}",
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
          ssl.libsDir(target).resolve("lib/libcrypto.a")
        }",
        "-DOPENSSL_SSL_LIBRARY=${
          ssl.libsDir(target).resolve("lib/libssl.a")
        }",
      )
    }

    cmakeArgs += "."

    commandLine(cmakeArgs)
  }

  compileSource { target ->
    outputs.dir(buildDir(target))
    commandLine(xtras.buildEnvironment.binaries.make, "install")
  }

  cinterops {
    headers = """
      package = $group.cinterops
			headers = MQTTAsync.h  MQTTClient.h  MQTTClientPersistence.h  MQTTExportDeclarations.h  MQTTProperties.h  MQTTReasonCodes.h  MQTTSubscribeOpts.h
		""".trimIndent()
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