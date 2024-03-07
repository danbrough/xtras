import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.openssl.openssl
import org.danbrough.xtras.ssh2.ssh2
import org.danbrough.xtras.xtrasTesting


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
  /*
  Will assume it's already built. We don't want this project building openssl
   */
  buildRequired.set { false }
}


val ssh2 = ssh2(ssl) {
}

kotlin {
  withSourcesJar(publish = true)
  applyDefaultHierarchyTemplate()
//  declareSupportedTargets()
  linuxX64()
  linuxArm64()

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
