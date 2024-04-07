import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
}

group = "org.danbrough.jni"
version = "0.0.1-alpha01"

kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  declareSupportedTargets()
  targets.withType<KotlinNativeTarget> {
    if (konanTarget.family != Family.ANDROID) {
      compilations["main"].cinterops {
        create("jni") {
          defFile(project.file("src/jni.def"))
          packageName = "platform.android"
          compilerOpts.add("-I${project.file("src/headers")}")
          when (konanTarget.family) {
            Family.LINUX -> "linux"
            Family.MINGW -> "win32"
            else -> error("Unhandled target: $konanTarget")
          }.also {
            compilerOpts.add("-I${project.file("src/headers/$it")}")
          }
        }
      }
    }
  }

}

xtrasTesting()

sonatype {
}
