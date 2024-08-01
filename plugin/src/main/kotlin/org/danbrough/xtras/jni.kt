package org.danbrough.xtras

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File

fun Project.xtrasAndroidConfig(
	namespace: String = group.toString(),
	compileSdk: Int = xtrasExtension.androidConfig.compileSDKVersion,
	block: LibraryExtension.() -> Unit = {}
) {
	extensions.getByType<LibraryExtension>().apply {
		//logWarn("$name::xtrasAndroidCOnfig() namespace=$namespace")
		this.compileSdk = compileSdk
		this.namespace = namespace

		defaultConfig {
			minSdk = xtrasExtension.androidConfig.minSDKVersion
			testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		}

		compileOptions {
			sourceCompatibility = xtrasExtension.javaVersion
			targetCompatibility = xtrasExtension.javaVersion
		}

		sourceSets["debug"].jniLibs {
			srcDir(project.file("src/jniLibs/debug"))
		}

		sourceSets["release"].jniLibs {
			srcDir(project.file("src/jniLibs/release"))
		}

		block()
	}

	afterEvaluate {
		/**
		 * Configure shared libraries to be copied to src/jniLibs/(debug|release)/(androidAbi)] after linking
		 */
		tasks.withType<KotlinNativeLink> {
			val konanTarget = binary.target.konanTarget
			if (konanTarget.family == Family.ANDROID) {
				val libsDir = outputs.files.files.first()
				val jniLibsDir =
					file("src/jniLibs/${if (binary.buildType.debuggable) "debug" else "release"}/${konanTarget.androidLibDir}")
				val taskCopyName = "${name}_copyToJniLibs"
				tasks.register<Copy>(taskCopyName) {
					doFirst {
						logDebug("copying files from $libsDir to $jniLibsDir for ${this@withType.name}")
					}
					from(libsDir) {
						include("lib*")
					}
					into(jniLibsDir)
				}
				finalizedBy(taskCopyName)
			}
		}


		/**
		 * Configure KotlinJvmTest task executions to find shared libraries
		 */

		val linkTasks = tasks.filter {
			it is KotlinNativeLink &&
					it.binary is SharedLibrary &&
					it.binary.target.konanTarget == HostManager.host
					&& it.binary.buildType == NativeBuildType.DEBUG
		}


		val libPath =
			project.pathOf(linkTasks.map{it.outputs.files.first()})

		logInfo("LIBPATH: $libPath linkTasks: ${linkTasks.joinToString{it.name}}")


		if (libPath.isBlank()) return@afterEvaluate

		tasks.withType<KotlinJvmTest> {
			logDebug("KotlinJvmTest:$name setting dependsOn(${linkTasks.joinToString{it.name}})")
			setDependsOn(linkTasks)
			environment(HostManager.host.envLibraryPathName, libPath)
			logInfo("jvm_test_task:$name setting env:${HostManager.host.envLibraryPathName} to ${environment[HostManager.host.envLibraryPathName]}")
		}

		tasks.withType<JavaExec> {
			logDebug("JavaExec:$name setting dependsOn(${linkTasks.joinToString{it.name}})")

			setDependsOn(linkTasks)
			environment(
				HostManager.host.envLibraryPathName,
				pathOf(libPath, environment[HostManager.host.envLibraryPathName])
			)
			logInfo("java_exec_task:$name setting env:${HostManager.host.envLibraryPathName} to ${environment[HostManager.host.envLibraryPathName]}")
		}
	}

}