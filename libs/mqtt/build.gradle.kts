import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.mqtt.mqtt
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest


plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.xtras)
	alias(libs.plugins.xtras.sonatype)
}

group = "org.danbrough.mqtt"
version = "0.0.1-alpha01"

xtras {
	buildEnvironment.binaries {
		//cmake = "/usr/bin/cmake"
	}
}


val ssl = openssl {
}


val mqtt = mqtt(ssl){
	buildEnabled = true
}




kotlin {
	withSourcesJar(publish = true)
	applyDefaultHierarchyTemplate()
	//declareSupportedTargets()
	linuxX64()
	macosX64()


	sourceSets {
		all {
			languageSettings {
				listOf(
					"kotlinx.cinterop.ExperimentalForeignApi",
					"kotlin.io.encoding.ExperimentalEncodingApi",
				).forEach(::optIn)
			}
		}

		val commonMain by getting {
			dependencies {
				implementation(project(":libs:support"))
				implementation(libs.kotlinx.coroutines)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}

		val nativeMain by getting {
		}
	}

/*	targets.withType<KotlinNativeTarget> {
		compilations["main"].cinterops {
			create("thang") {
				definitionFile = file("src/cinterops/thang.def")
				compilerOpts("-I${project.file("src/cinterops")}")
				afterEvaluate {
					tasks.getByName(interopProcessingTaskName).apply {
						inputs.file(project.file("src/cinterops/thang.h"))
					}
				}
			}
		}
	}*/


}

xtrasTesting()

sonatype {
}

afterEvaluate {
	tasks.withType<KotlinNativeHostTest> {


	}
}
