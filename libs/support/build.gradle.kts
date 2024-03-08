import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.runningInIDE
import org.danbrough.xtras.xtrasTesting


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
}

group = "$XTRAS_PACKAGE.support"

xtras {
  buildEnvironment.binaries {
    //cmake = "/usr/bin/cmake"
  }
}


kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
  if (runningInIDE) {
    declareHostTarget()
  } else {
    declareSupportedTargets()
  }


  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlin.logging)
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
