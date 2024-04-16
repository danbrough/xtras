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
/*

fun Project.openssl(
  zlib: LibraryExtension = zlib { },
  block: LibraryExtension.() -> Unit = {},
): LibraryExtension =
  registerGitLibrary("openssl") {
    dependsOn(zlib)

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

    compileSource { target ->
      val buildDir = buildDir(target)
      outputs.dir(buildDir)
      doFirst {
        environment.keys.sorted().forEach {
          project.logWarn("ENV: $it: ${environment[it]}")
        }
      }
      commandLine("make")
    }

    installSource {
      commandLine("make", "install_sw")
    }

    cinterops {
      headers = """

        #staticLibraries =  libcrypto.a libssl.a
        headerFilter = openssl/**
        #headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
        excludeDependentModules = true
        linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
        linkerOpts.mingw = -lm -lssl -lcrypto
        compilerOpts.android = -D__ANDROID_API__=${xtras.buildEnvironment.androidNdkApiVersion}
        compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
        #compilerOpts = -static
        """.trimIndent()
    }



    block()
  }


/*
const val PROPERTY_OPENSSL_VERSION = "xtras.openssl.version"
const val PROPERTY_OPENSSL_COMMIT = "xtras.openssl.commit"

object OpenSSL {
  const val group = "$XTRAS_PACKAGE.openssl"
  const val extensionName = "openSSL"
  var extensionVersion = "3.1.3"
  const val sourceURL = "https://github.com/openssl/openssl.git"
  var sourceCommit = "openssl-3.1.3"
}

@XtrasDSL
fun Project.openssl(
  group: String = OpenSSL.group,
  name: String = OpenSSL.extensionName,
  version: String = projectProperty<String>(PROPERTY_OPENSSL_VERSION, OpenSSL.extensionVersion),
  sourceURL: String = OpenSSL.sourceURL,
  sourceCommit: String = projectProperty<String>(PROPERTY_OPENSSL_COMMIT, OpenSSL.sourceCommit),
  configure: XtrasLibrary.() -> Unit = {}
): XtrasLibrary = registerLibrary(group, name, version) {
  log("registeredLibrary: $name for ${this@openssl.name}", LogLevel.INFO)

  gitSource(sourceURL, sourceCommit)
  cinterops {
    headers = """
      #staticLibraries =  libcrypto.a libssl.a
      headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
      linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto
      linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
      linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
      linkerOpts.mingw = -lm -lssl -lcrypto
      compilerOpts.android = -D__ANDROID_API__=21
      compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
      #compilerOpts = -static

          """.trimIndent()
  }

  configureTargetTasks = { configureOpenSSLTasks(it) }
  configure()
}


internal fun XtrasLibrary.configureOpenSSLTasks(target: KonanTarget) {
  val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
    dependsOn(extractSourceTaskName(target))
    outputs.file(workingDir.resolve("Makefile"))
    val args = mutableListOf(
      "./Configure",
      target.opensslPlatform,
      "no-tests",
      "threads",
      "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}",
      "--libdir=lib",
    )

    if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
    /*      else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"
          environment("CFLAGS", "  -Wno-macro-redefined ")*/

    if (HostManager.hostIsMingw) commandLine(
      buildEnvironment.binaries.bash, "-c", args.joinToString(" ")
    )
    else commandLine(args)
  }



  xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
    doFirst {
      project.log("running make install with CC=${environment["CC"]}")
    }
    dependsOn(configureTask)
    outputs.dir(buildDir(target))
    commandLine("make", "install_sw")
  }
}





 */

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
pick os/compiler from:
BC-32 BS2000-OSD BSD-aarch64 BSD-armv4 BSD-generic32 BSD-generic64 BSD-ia64
BSD-ppc BSD-ppc64 BSD-ppc64le BSD-riscv32 BSD-riscv64 BSD-sparc64 BSD-sparcv8
BSD-x86 BSD-x86-elf BSD-x86_64 Cygwin Cygwin-i386 Cygwin-i486 Cygwin-i586
Cygwin-i686 Cygwin-x86 Cygwin-x86_64 DJGPP MPE/iX-gcc OS390-Unix UEFI UEFI-x86
UEFI-x86_64 UWIN VC-CE VC-CLANG-WIN64-CLANGASM-ARM VC-WIN32 VC-WIN32-ARM
VC-WIN32-ARM-UWP VC-WIN32-ONECORE VC-WIN32-UWP VC-WIN64-ARM VC-WIN64-ARM-UWP
VC-WIN64-CLANGASM-ARM VC-WIN64A VC-WIN64A-ONECORE VC-WIN64A-UWP VC-WIN64A-masm
VC-WIN64I aix-cc aix-gcc aix64-cc aix64-gcc aix64-gcc-as android-arm
android-arm64 android-armeabi android-mips android-mips64 android-x86
android-x86_64 android64 android64-aarch64 android64-mips64 android64-x86_64
bsdi-elf-gcc cc darwin-i386 darwin-i386-cc darwin-ppc darwin-ppc-cc
darwin64-arm64 darwin64-arm64-cc darwin64-ppc darwin64-ppc-cc darwin64-x86_64
darwin64-x86_64-cc gcc haiku-x86 haiku-x86_64 hpux-ia64-cc hpux-ia64-gcc
hpux-parisc-cc hpux-parisc-gcc hpux-parisc1_1-cc hpux-parisc1_1-gcc
hpux64-ia64-cc hpux64-ia64-gcc hpux64-parisc2-cc hpux64-parisc2-gcc hurd-x86
ios-cross ios-xcrun ios64-cross ios64-xcrun iossimulator-xcrun iphoneos-cross
irix-mips3-cc irix-mips3-gcc irix64-mips4-cc irix64-mips4-gcc linux-aarch64
linux-alpha-gcc linux-aout linux-arm64ilp32 linux-armv4 linux-c64xplus
linux-elf linux-generic32 linux-generic64 linux-ia64 linux-latomic
linux-mips32 linux-mips64 linux-ppc linux-ppc64 linux-ppc64le linux-sparcv8
linux-sparcv9 linux-x32 linux-x86 linux-x86-clang linux-x86_64
linux-x86_64-clang linux32-riscv32 linux32-s390x linux64-loongarch64
linux64-mips64 linux64-riscv64 linux64-s390x linux64-sparcv9 mingw mingw64
nonstop-nse nonstop-nse_64 nonstop-nse_64_put nonstop-nse_g
nonstop-nse_g_tandem nonstop-nse_put nonstop-nse_spt nonstop-nse_spt_floss
nonstop-nsv nonstop-nsx nonstop-nsx_64 nonstop-nsx_64_put nonstop-nsx_g
nonstop-nsx_g_tandem nonstop-nsx_put nonstop-nsx_spt nonstop-nsx_spt_floss
sco5-cc sco5-gcc solaris-sparcv7-cc solaris-sparcv7-gcc solaris-sparcv8-cc
solaris-sparcv8-gcc solaris-sparcv9-cc solaris-sparcv9-gcc solaris-x86-gcc
solaris64-sparcv9-cc solaris64-sparcv9-gcc solaris64-x86_64-cc
solaris64-x86_64-gcc tru64-alpha-cc tru64-alpha-gcc uClinux-dist
uClinux-dist64 unixware-2.0 unixware-2.1 unixware-7 unixware-7-gcc vms-alpha
vms-alpha-p32 vms-alpha-p64 vms-ia64 vms-ia64-p32 vms-ia64-p64 vms-x86_64
vms-x86_64-cross-ia64 vos-gcc vxworks-mips vxworks-ppc405 vxworks-ppc60x
vxworks-ppc750 vxworks-ppc750-debug vxworks-ppc860 vxworks-ppcgen
vxworks-simlinux
    */


/*
For OpenSSL 1.1.0 , a 64-bit iOS cross-compiles uses the ios64-cross target, and --prefix=/usr/local/openssl-ios64. ios64-cross. There is no built-in 64-bit iOS support for OpenSSL 1.0.2 or below.

$ export CC=clang;
$ export CROSS_TOP=/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer
$ export CROSS_SDK=iPhoneOS.sdk
$ export PATH="/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin:$PATH"

$ ./Configure ios64-cross no-shared no-dso no-hw no-engine --prefix=/usr/local/openssl-ios64

Configuring OpenSSL version 1.1.1-dev (0x10101000L)
    no-afalgeng     [forced]   OPENSSL_NO_AFALGENG
    no-asan         [default]  OPENSSL_NO_ASAN
    no-dso          [option]
    no-dynamic-engine [forced]
    ...
    no-weak-ssl-ciphers [default]  OPENSSL_NO_WEAK_SSL_CIPHERS
    no-zlib         [default]
    no-zlib-dynamic [default]
Configuring for ios64-cross

PERL          =perl
PERLVERSION   =5.16.2 for darwin-thread-multi-2level
HASHBANGPERL  =/usr/bin/env perl
CC            =clang
CFLAG         =-O3 -D_REENTRANT -arch arm64 -mios-version-min=7.0.0 -isysroot $(CROSS_TOP)/SDKs/$(CROSS_SDK) -fno-common
CXX           =c++
CXXFLAG       =-O3 -D_REENTRANT -arch arm64 -mios-version-min=7.0.0 -isysroot $(CROSS_TOP)/SDKs/$(CROSS_SDK) -fno-common
DEFINES       =NDEBUG OPENSSL_THREADS OPENSSL_NO_DYNAMIC_ENGINE OPENSSL_PIC OPENSSL_BN_ASM_MONT SHA1_ASM SHA256_ASM SHA512_ASM VPAES_ASM ECP_NISTZ256_ASM POLY1305_ASM
...
 */

 */