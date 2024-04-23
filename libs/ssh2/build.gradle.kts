@file:OptIn(ExperimentalKotlinGradlePluginApi::class)


import org.danbrough.xtras.core.openssl
import org.danbrough.xtras.core.ssh2
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.xtrasJniConfig
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasTestExecutables
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
  //`maven-publish`
}

buildscript {
  dependencies {
    classpath(libs.xtras.core)
  }
}

xtras {
  kotlinApiVersion = KotlinVersion.KOTLIN_2_0
  kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0
  jvmTarget = JvmTarget.JVM_17
  javaVersion = JavaVersion.VERSION_17
  cleanEnvironment = true
}

group = projectProperty<String>("ssh2.group")
version = projectProperty<String>("ssh2.version")


kotlin {
  withSourcesJar(publish = true)

  compilerOptions {
    freeCompilerArgs = listOf("-Xexpect-actual-classes")
    languageVersion = xtras.kotlinLanguageVersion
    apiVersion = xtras.kotlinApiVersion
  }

  applyDefaultHierarchyTemplate()

  jvm()

  androidTarget {
  }

  linuxX64()
  linuxArm64()
  mingwX64()

  if (HostManager.hostIsMac) {
    macosX64()
    //macosArm64()
  }

  androidNativeArm64()
  androidNativeX86()
  androidNativeX64()

  sourceSets {
    all {
      languageSettings {
        listOf(
          "kotlin.ExperimentalStdlibApi",
          "kotlin.io.encoding.ExperimentalEncodingApi",
          "kotlin.experimental.ExperimentalNativeApi",
          "kotlinx.cinterop.ExperimentalForeignApi",
        ).forEach(::optIn)
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(libs.xtras.support) //or implementation(project(":libs:support"))
        implementation(libs.kotlinx.coroutines)
        //implementation(project(":libs:openssl"))
        //implementation(libs.kotlinx.io)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    val jniMain by creating {
      dependsOn(commonMain)
    }

    jvmMain {
      dependsOn(jniMain)
    }

    jvmTest {
      dependencies {
        implementation(kotlin("stdlib"))
      }
    }

    androidMain {
      dependsOn(jniMain)
    }
  }

  targets.withType<KotlinNativeTarget> {
    binaries {
      sharedLib("xtras_ssh2")
//        executable("sshExec") {
//          entryPoint = "org.danbrough.ssh2.mainSshExec"
//          compilation = compilations.getByName("test")
//        }
    }
  }
}



xtrasTestExecutables("ssh", tests = listOf("sshExec")) {
  it.family == Family.LINUX
}

xtrasTesting {

}

sonatype {
}

xtrasJniConfig {
  compileSdk = 34
}

/*rootProject.findProject(":libs:openssl")!!.also {
  val openssl = it.extensions.getByType<XtrasLibrary>()
  logError("LIBS DIR OPENSSL: ${openssl.libsDir(KonanTarget.LINUX_ARM64)}")
}*/


ssh2 {
  cinterops {
    codeFile = file("interops.h")
  }

  extraLibsDirs += {
    xtrasLibsDir.resolveAll(
      "openssl",
      projectProperty<String>("openssl.version"),
      it.kotlinTargetName
    )
  }
}

val ssl = openssl {
}

tasks.register("printSSL") {
  doFirst {
    println("${project.name}: buildEnabled: ${ssl.buildEnabled}")
  }
}

