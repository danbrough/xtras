import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.xtrasTesting
import org.danbrough.xtras.zlib.zlib


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
}

group = "org.danbrough.openssl"
version = "3.2.1-beta01"

val zlibDependency = zlib()


openssl(zlibDependency) {
  buildEnabled = true
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
    val commonMain by getting {
      dependencies {
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
}

xtrasTesting()

sonatype {

}
