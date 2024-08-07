@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("UnstableApiUsage")


import org.danbrough.xtras.core.openssl
import org.danbrough.xtras.core.ssh2
import org.danbrough.xtras.envLibraryPathName
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.logError
import org.danbrough.xtras.logInfo
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.sonatype.xtrasSonatype
import org.danbrough.xtras.supportsJNI
import org.danbrough.xtras.xtras
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasTestExecutables
import org.danbrough.xtras.xtrasTesting
import org.danbrough.xtras.xtrasSharedLibs
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	id("com.android.library")
	//`maven-publish`
}

buildscript {
	dependencies {
		classpath(libs.xtras.core)
	}
}


group = projectProperty<String>("ssh2.group")
version = projectProperty<String>("ssh2.version")

xtras {
	androidConfig {
		ndkApiVersion = 23
		minSDKVersion = 23
	}
}

kotlin {
	withSourcesJar(publish = true)

	compilerOptions {
		freeCompilerArgs = listOf("-Xexpect-actual-classes")
		languageVersion = xtras.kotlinLanguageVersion
		apiVersion = xtras.kotlinApiVersion
	}

	applyDefaultHierarchyTemplate()

	jvm()

	androidTarget {
	}


	if (HostManager.hostIsMingw) {
		mingwX64()
	} else {
		linuxX64()
		mingwX64()
		linuxArm64()
		androidNativeArm64()
		androidNativeX64()
		if (HostManager.hostIsMac) {
			macosArm64()
			macosX64()
		}
	}

	sourceSets {
		all {
			languageSettings {
				listOf(
					"kotlin.ExperimentalStdlibApi",
					"kotlin.io.encoding.ExperimentalEncodingApi",
					"kotlin.experimental.ExperimentalNativeApi",
					"kotlinx.cinterop.ExperimentalForeignApi",
				).forEach(::optIn)
			}
		}

		val commonMain by getting {
			dependencies {
				implementation(project(":libs:support"))
				implementation(libs.kotlinx.coroutines)
				implementation(libs.klog.core)
			}
		}

		commonTest {
			dependencies {
				implementation(kotlin("test"))
				implementation(libs.kotlinx.datetime)
			}
		}

		val jniMain by creating {
			dependsOn(commonMain)
		}

		jvmMain {
			dependsOn(jniMain)
		}

		jvmTest {
			dependencies {
				implementation(kotlin("stdlib"))
			}
		}

		androidMain {
			dependsOn(jniMain)
		}
	}

	val nativeMain by sourceSets.getting {
		dependencies {
			implementation(libs.kotlinx.io)
		}
	}

	val posixMain by sourceSets.creating {
		dependsOn(nativeMain)
	}

	val posixTest by sourceSets.creating {
		dependsOn(sourceSets.getAt("nativeTest"))
	}

	targets.withType<KotlinNativeTarget> {

		compilations["main"].apply {
			defaultSourceSet {
				if (konanTarget.supportsJNI)
					kotlin.srcDir(project.file("src").resolve("jni"))
				dependsOn(posixMain)
			}
		}

		compilations["test"].apply {
			defaultSourceSet {
				dependsOn(posixTest)
			}
		}

		binaries {
			sharedLib("xtras_ssh2")
		}
	}
}



xtrasTestExecutables("ssh", tests = listOf("sshExec", "sshExec2")) {
	runTask?.apply {
		val sallyKeyFile = file("docker/sally.key")
		if (!sallyKeyFile.exists()) error(
			"""${sallyKeyFile.absolutePath} not found. 
					|You need to run ${file("docker/docker.sh")} first""".trimMargin()
		)
		environment("SSH_PRIVATE_KEY", sallyKeyFile.absolutePath)
	}
}

xtrasTesting {
	if (this is KotlinNativeTest) {
		doFirst {
			val sallyKeyFile = file("docker/sally.key")
			if (!sallyKeyFile.exists()) error(
				"""${sallyKeyFile.absolutePath} not found. 
					|You need to run ${file("docker/docker.sh")} first""".trimMargin()
			)
			environment("SSH_PRIVATE_KEY", sallyKeyFile.absolutePath)
		}
	}
}


/*afterEvaluate {
  tasks.withType<Exec> {
    logWarn("EXEC: ${name} type: ${this::class.java}")
    val sallyKeyFile = file("docker/sally.key")
    if (!sallyKeyFile.exists()) error(
      """${sallyKeyFile.absolutePath} not found. 
					|You need to run ${file("docker/docker.sh")} first""".trimMargin()
    )
    environment("SSH_PRIVATE_KEY", sallyKeyFile.absolutePath)
  }
}*/

xtrasSonatype {
}

xtrasAndroidConfig {
}

val ssl = openssl {
/*	afterEvaluate {
		tasks.withType<KotlinNativeTest>{
			println("NATIVE TEST: $name ${this::class.java}")
		}

		tasks.withType<Exec>{
			println("EXEC: $name ${this::class.java}")
		}
	}*/
}


ssh2(ssl) {
	cinterops {
		codeFile = file("src/cinterops/interops.h")
	}
}


