import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.xtras)
	id("org.danbrough.xtras.sonatype")

}

group = "org.danbrough.jni"
version = "0.0.1-alpha01"

xtras {
	buildEnvironment.binaries {
		//cmake = "/usr/bin/cmake"
	}
}




kotlin {
	withSourcesJar(publish = true)
	applyDefaultHierarchyTemplate()
	linuxX64()

}

xtrasTesting()

sonatype {
}
