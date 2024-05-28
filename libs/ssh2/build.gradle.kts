@file:OptIn(ExperimentalKotlinGradlePluginApi::class)


import org.danbrough.xtras.core.openssl
import org.danbrough.xtras.core.ssh2
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.supportsJNI
import org.danbrough.xtras.xtrasAndroidConfig
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasTestExecutables
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.xtras)
	id("org.danbrough.xtras.sonatype")
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

	mingwX64()


/*		linuxX64()
		mingwX64()
		linuxArm64()
		androidNativeArm64()
		androidNativeX64()
		if (HostManager.hostIsMac) {
			macosArm64()
			macosX64()
		}
	*/

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

	targets.withType<KotlinNativeTarget> {
		if (konanTarget.supportsJNI)
			compilations["main"].defaultSourceSet.kotlin.srcDir(project.file("src").resolve("jni"))
		binaries {
			sharedLib("xtras_ssh2")
//        executable("sshExec") {
//          entryPoint = "org.danbrough.ssh2.mainSshExec"
//          compilation = compilations.getByName("test")
//        }
		}
	}
}



xtrasTestExecutables("ssh", tests = listOf("sshExec")){
	it == HostManager.host || it == KonanTarget.MINGW_X64
}

xtrasTesting {

}

sonatype {
}

xtrasAndroidConfig {
}

val ssl = openssl {
}


ssh2(ssl) {
	cinterops {
		codeFile = file("src/cinterops/interops.h")
	}
}


