@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("SpellCheckingInspection")


import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.konanDir
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.resolveAll
import org.danbrough.xtras.tasks.PackageTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.danbrough.xtras.xtrasCommandLine
import org.danbrough.xtras.xtrasJniConfig
import org.danbrough.xtras.xtrasTesting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.xtras.sonatype")
  id("com.android.library")
}

xtras {
  kotlinApiVersion = KotlinVersion.KOTLIN_2_0
  kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0
  jvmTarget = JvmTarget.JVM_17
  javaVersion = JavaVersion.VERSION_17
  cleanEnvironment = true

}

group = projectProperty<String>("openssl.group")
version = projectProperty<String>("openssl.version")

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
  androidNativeArm64()
  macosX64()
  macosArm64()

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
        //implementation(libs.kotlinx.coroutines)
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
      sharedLib("xtras_openssl")
    }

  }
}

/*xtrasEnableTestExes("ssh", tests = listOf("execTest")) {
  it in setOf(KonanTarget.LINUX_X64, KonanTarget.MINGW_X64, KonanTarget.LINUX_ARM64)
}*/



xtrasTesting {
}

sonatype {
}

xtrasJniConfig {
  compileSdk = 34
}

registerXtrasGitLibrary<XtrasLibrary>("openssl") {

  environment { target ->
    if (target.family == Family.ANDROID)
      put("ANDROID_NDK_ROOT", xtras.androidConfig.ndkDir)
  }

  cinterops {
    declaration = """
        #staticLibraries =  libcrypto.a libssl.a
        headerFilter = openssl/**
        #headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
        excludeDependentModules = true
        linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto -L/usr/lib 
        linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.mingw = -lm -lssl -lcrypto
        compilerOpts.android = -D__ANDROID_API__=${xtras.androidConfig.ndkApiVersion}  
        compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        #compilerOpts = -static
        
        """.trimIndent()

    targetWriterFilter = { target -> target == KonanTarget.LINUX_ARM64 }

    afterEvaluate {
      tasks.withType<CInteropProcess> {
        if (konanTarget in setOf(KonanTarget.LINUX_ARM64)) {
          dependsOn(PackageTaskName.EXTRACT.taskName(this@registerXtrasGitLibrary, konanTarget))
        }
      }
    }
  }

  environment { target ->
    put("MAKEFLAGS", "-j8")
    put("CFLAGS", "-Wno-unused-command-line-argument -Wno-macro-redefined")

    if (target == KonanTarget.LINUX_ARM64) {
      //put("PATH",pathOf(project.xtrasKon))

      val depsDir = project.konanDir.resolve("dependencies")
      val llvmPrefix = if (HostManager.hostIsLinux) "llvm-" else "apple-llvm"
      val llvmDir = depsDir.listFiles()?.first {
        it.isDirectory && it.name.startsWith(llvmPrefix)
      } ?: error("No directory beginning with \"llvm-\" found in ${depsDir.mixedPath}")
      put("PATH", pathOf(llvmDir.resolve("bin"), get("PATH")))
      val clangArgs =
        "--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")}" +
            " --sysroot=${
              depsDir.resolveAll(
                "aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2",
                "aarch64-unknown-linux-gnu",
                "sysroot"
              )
            }"
      put("CLANG_ARGS", clangArgs)
      put("CC", "clang $clangArgs")
      put("CXX", "clang++ $clangArgs")
    } else if (target == KonanTarget.MINGW_X64) {
      /*put("CC", "x86_64-w64-mingw32-gcc")
      put("AR", "x86_64-w64-mingw32-ar")
      put("RANLIB", "x86_64-w64-mingw32-ranlib")
      put("RC", "x86_64-w64-mingw32-windres")*/
      //put("PREFIX", "x86_64-w64-mingw32-")
    }
  }

  configureSource { target ->
    outputs.file(workingDir.resolve("Makefile"))

    val args = mutableListOf(
      "./Configure",
      target.opensslPlatform,
      "no-tests",
      "threads",
      "zlib",
//      "--with-zlib-include=${zlib.libsDir(target).resolve("include")}",
//      "--with-zlib-lib=${zlib.libsDir(target).resolve("lib").resolve("libz.a")}",
      "--prefix=${buildDir(target)}",
      "--libdir=lib",
    )

    if (target.family == Family.ANDROID)
      args += "-D__ANDROID_API__=${xtras.androidConfig.ndkApiVersion}"
    else if (target == KonanTarget.MINGW_X64) {
      args += "--cross-compile-prefix=x86_64-w64-mingw32-"
    }


    xtrasCommandLine(args)
  }

  compileSource {
    xtrasCommandLine("make")
  }

  installSource {
    xtrasCommandLine("make", "install_sw")
  }


  /*
     configureSource { target ->
      val makeFile = workingDir.resolve("Makefile")
      outputs.file(makeFile)


      if (target.family == Family.ANDROID)
        environment("ANDROID_NDK_ROOT", xtras.buildEnvironment.androidNdkDir.absolutePath)
      else if (target.family == Family.MINGW) {
        if (!HostManager.hostIsMingw) {
          environment("CC", "x86_64-w64-mingw32-gcc")
          environment("AR", "x86_64-w64-mingw32-ar")
          environment("RANLIB", "x86_64-w64-mingw32-ranlib")
          environment("RC", "x86_64-w64-mingw32-windres")
        }
      }

      environment("CFLAGS", "-Wno-macro-redefined")

      doFirst {
        project.logWarn("RUNNING CONFIGURE WITH ${commandLine.joinToString(" ")} CFLAGS: ${environment["CFLAGS"]}")
      }
      onlyIf {
        !makeFile.exists()
      }

      val args = mutableListOf(
        "./Configure",
        target.opensslPlatform,
        "no-tests",
        "threads",
        "zlib",
        "--with-zlib-include=${zlib.libsDir(target).resolve("include")}",
        "--with-zlib-lib=${zlib.libsDir(target).resolve("lib").resolve("libz.a")}",
        "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}",
        "--libdir=lib",
      )


      if (target.family == Family.MINGW)
        args += "no-zlib-dynamic"

      if (target.family == Family.ANDROID) {
        args += "-D__ANDROID_API__=${xtras.buildEnvironment.androidNdkApiVersion}"
      }

      commandLine(args)
    }
   */
  /*  prepareSource { target ->
      val args = if (target.family == Family.MINGW)
        listOf("sh", project.xtrasMsysDir.subDir("usr", "bin", "autoreconf"), "-fi")
      else listOf("autoreconf", "-fi")
      xtrasCommandLine(args)
      outputs.file(workingDir.resolve("configure"))
    }

    configureSource(dependsOn = SourceTaskName.PREPARE) { target ->
      outputs.file(workingDir.resolve("Makefile"))

      val args = mutableListOf(
        "sh",
        "./configure",
        //"--enable-examples-build",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target).mixedPath}",
        "--with-libz"
      )
      xtrasCommandLine(args)
    }

    compileSource { target ->
      xtrasCommandLine("make")
    }

    installSource { target ->
      xtrasCommandLine("make", "install")

      doLast {
        copy {
          from(workingDir.resolve("example/.libs")) {
            include {
              @Suppress("UnstableApiUsage")
              !it.isDirectory && it.permissions.user.execute
            }
          }
          into(buildDir(target).resolve("bin"))
        }
      }
    }*/

}

val KonanTarget.opensslPlatform: String
  get() = when (this) {
    KonanTarget.LINUX_X64 -> "linux-x86_64"
    KonanTarget.LINUX_ARM64 -> "linux-aarch64"
    //  KonanTarget.LINUX_ARM32_HFP -> "linux-armv4"
//    KonanTarget.LINUX_MIPS32 -> TODO()
//    KonanTarget.LINUX_MIPSEL32 -> TODO()
    KonanTarget.ANDROID_ARM32 -> "android-arm"
    KonanTarget.ANDROID_ARM64 -> "android-arm64"
    KonanTarget.ANDROID_X86 -> "android-x86"
    KonanTarget.ANDROID_X64 -> "android-x86_64"
    KonanTarget.MINGW_X64 -> "mingw64"
    //KonanTarget.MINGW_X86 -> "mingw"


    KonanTarget.MACOS_X64 -> "darwin64-x86_64"
    KonanTarget.MACOS_ARM64 -> "darwin64-arm64-cc"
    //KonanTarget.IOS_ARM32 -> "ios-cross" //ios-cross ios-xcrun ios64-cross ios64-xcrun iossimulator-xcrun iphoneos-cross

    KonanTarget.IOS_ARM64 -> "ios64-cross" //ios-cross ios-xcrun
    //KonanTarget.IOS_SIMULATOR_ARM64 -> "iossimulator-xcrun"
    KonanTarget.IOS_X64 -> "ios64-cross"

    else -> throw Error("$this not supported for openssl")
  }

/*
Usage: Configure [no-<feature> ...] [enable-<feature> ...]
[-Dxxx] [-lxxx] [-Lxxx] [-fxxx] [-Kxxx] [no-hw-xxx|no-hw]
[[no-]threads] [[no-]thread-pool] [[no-]default-thread-pool]
[[no-]shared] [[no-]zlib|zlib-dynamic]
[no-asm] [no-egd] [sctp] [386] [--prefix=DIR]
[--openssldir=OPENSSLDIR] [--with-xxx[=vvv]] [--config=FILE] os/compiler[:flags]

pick os/compiler from:
BC-32 BS2000-OSD BSD-aarch64 BSD-armv4 BSD-generic32 BSD-generic64 BSD-ia64
BSD-nodef-generic32 BSD-nodef-generic64 BSD-nodef-ia64 BSD-nodef-sparc64
BSD-nodef-sparcv8 BSD-nodef-x86 BSD-nodef-x86-elf BSD-nodef-x86_64 BSD-ppc
BSD-ppc64 BSD-ppc64le BSD-riscv32 BSD-riscv64 BSD-sparc64 BSD-sparcv8 BSD-x86
BSD-x86-elf BSD-x86_64 Cygwin Cygwin-i386 Cygwin-i486 Cygwin-i586 Cygwin-i686
Cygwin-x86 Cygwin-x86_64 DJGPP MPE/iX-gcc OS390-Unix UEFI UEFI-x86 UEFI-x86_64
UWIN VC-CE VC-CLANG-WIN64-CLANGASM-ARM VC-WIN32 VC-WIN32-ARM VC-WIN32-ARM-UWP
VC-WIN32-HYBRIDCRT VC-WIN32-ONECORE VC-WIN32-UWP VC-WIN64-ARM VC-WIN64-ARM-UWP
VC-WIN64-CLANGASM-ARM VC-WIN64A VC-WIN64A-HYBRIDCRT VC-WIN64A-ONECORE
VC-WIN64A-UWP VC-WIN64A-masm VC-WIN64I aix-cc aix-cc-solib aix-gcc aix64-cc
aix64-cc-solib aix64-gcc aix64-gcc-as android-arm android-arm64
android-armeabi android-mips android-mips64 android-riscv64 android-x86
android-x86_64 android64 android64-aarch64 android64-mips64 android64-x86_64
bsdi-elf-gcc cc darwin-i386 darwin-i386-cc darwin-ppc darwin-ppc-cc
darwin64-arm64 darwin64-arm64-cc darwin64-ppc darwin64-ppc-cc darwin64-x86_64
darwin64-x86_64-cc gcc haiku-x86 haiku-x86_64 hpux-ia64-cc hpux-ia64-gcc
hpux-parisc-cc hpux-parisc-gcc hpux-parisc1_1-cc hpux-parisc1_1-gcc
hpux64-ia64-cc hpux64-ia64-gcc hpux64-parisc2-cc hpux64-parisc2-gcc
hurd-generic32 hurd-generic64 hurd-x86 hurd-x86_64 ios-cross ios-xcrun
ios64-cross ios64-xcrun iossimulator-arm64-xcrun iossimulator-i386-xcrun
iossimulator-x86_64-xcrun iossimulator-xcrun iphoneos-cross irix-mips3-cc
irix-mips3-gcc irix64-mips4-cc irix64-mips4-gcc linux-aarch64 linux-alpha-gcc
linux-aout linux-arm64ilp32 linux-armv4 linux-c64xplus linux-elf
linux-generic32 linux-generic64 linux-ia64 linux-latomic linux-mips32
linux-mips64 linux-ppc linux-ppc64 linux-ppc64le linux-sparcv8 linux-sparcv9
linux-x32 linux-x86 linux-x86-clang linux-x86-latomic linux-x86_64
linux-x86_64-clang linux32-riscv32 linux32-s390x linux64-loongarch64
linux64-mips64 linux64-riscv64 linux64-s390x linux64-sparcv9 mingw mingw64
nonstop-nse nonstop-nse_64 nonstop-nse_64_put nonstop-nse_g
nonstop-nse_g_tandem nonstop-nse_put nonstop-nsv nonstop-nsx nonstop-nsx_64
nonstop-nsx_64_put nonstop-nsx_g nonstop-nsx_g_tandem nonstop-nsx_put sco5-cc
sco5-gcc solaris-sparcv7-cc solaris-sparcv7-gcc solaris-sparcv8-cc
solaris-sparcv8-gcc solaris-sparcv9-cc solaris-sparcv9-gcc solaris-x86-gcc
solaris64-sparcv9-cc solaris64-sparcv9-gcc solaris64-x86_64-cc
solaris64-x86_64-gcc tru64-alpha-cc tru64-alpha-gcc uClinux-dist
uClinux-dist64 unixware-2.0 unixware-2.1 unixware-7 unixware-7-gcc vms-alpha
vms-alpha-p32 vms-alpha-p64 vms-ia64 vms-ia64-p32 vms-ia64-p64 vms-x86_64
vms-x86_64-cross-ia64 vms-x86_64-p32 vms-x86_64-p64 vos-gcc vxworks-mips
vxworks-ppc405 vxworks-ppc60x vxworks-ppc750 vxworks-ppc750-debug
vxworks-ppc860 vxworks-ppcgen vxworks-simlinux
 */
