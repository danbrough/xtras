import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.xtrasTesting
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.tasks.gitSource
import org.gradle.jvm.tasks.Jar


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
}

group = "$XTRAS_PACKAGE.openssl"

val ssl = openssl {
}


/*
mqtt {
  dependsOn(ssl)
}
*/


xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}

kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  declareSupportedTargets()

  sourceSets {
    val commonMain by getting
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val nativeMain by getting {
    }
  }
}

xtrasTesting()

sonatype {

}
