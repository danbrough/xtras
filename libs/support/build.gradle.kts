import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.declareHostTarget
import org.danbrough.xtras.declareSupportedTargets
import org.danbrough.xtras.runningInIDE
import org.danbrough.xtras.xtrasTesting

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("com.android.library")
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
  jvm()
  androidTarget()


  sourceSets {

    val commonMain by getting {
      dependencies {
        api(libs.kotlin.logging)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    val nativeMain by getting {
    }

    jvmMain {
      dependencies {
        api(libs.slf4j.api)
        implementation(libs.slf4j.simple)
      }
    }

    androidMain {
      dependencies {
        api(libs.slf4j.api)
        implementation(libs.slf4j.simple)
      }
    }
  }
}

android {
  compileSdk = 34
  namespace = "$XTRAS_PACKAGE.support"
  defaultConfig {
    minSdk = 22
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility =  JavaVersion.VERSION_1_8
  }
}

xtrasTesting()

sonatype {

}
