import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.platformName
import org.danbrough.xtras.runningInIDE
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
}

group = "$XTRAS_PACKAGE.libssh2"


xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


val ssl = openssl {

}


val ssh2 = ssh2(ssl) {
}

kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  declareSupportedTargets()


  sourceSets {
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

  targets.withType<KotlinNativeTarget> {
    compilations["main"].cinterops {
      create("thang") {
        definitionFile = file("test.def")
      }
    }
  }
}

xtrasTesting()

sonatype {

}
