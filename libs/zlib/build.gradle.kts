import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.xtrasTesting
import org.danbrough.xtras.zlib.zlib


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
}

group = "org.danbrough.zlib"
version = "1.3.1"


zlib {
  buildEnabled = true
}


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
