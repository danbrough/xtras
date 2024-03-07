package org.danbrough.xtras.openssl

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.logWarn
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.gitSource
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

const val OPENSSL_EXTN_NAME = "openssl"
const val PROPERTY_OPENSSL_GROUP = "openssl.group"
const val PROPERTY_OPENSSL_VERSION = "openssl.version"
const val PROPERTY_OPENSSL_COMMIT = "openssl.commit"
const val PROPERTY_OPENSSL_URL = "openssl.url"


abstract class OpenSSLLibrary(group: String, name: String, version: String, project: Project) :
  LibraryExtension(group, name, version, project)


fun Project.openssl(
  group: String = projectProperty<String>(PROPERTY_OPENSSL_GROUP),
  version: String = projectProperty<String>(PROPERTY_OPENSSL_VERSION),
  url: String = projectProperty<String>(PROPERTY_OPENSSL_URL),
  commit: String = projectProperty<String>(PROPERTY_OPENSSL_COMMIT),
  block: OpenSSLLibrary.() -> Unit
): OpenSSLLibrary =
  extensions.findByType<OpenSSLLibrary>()?.also {
    project.extensions.configure<OpenSSLLibrary>(block)
  } ?: xtrasRegisterLibrary<OpenSSLLibrary>(group, OPENSSL_EXTN_NAME, version) {

    gitSource(url, commit)

    configureSource { target ->
      val makeFile = workingDir.resolve("Makefile")
      outputs.file(makeFile)

      environment("CFLAGS", "-Wno-macro-redefined")
      doFirst {
        project.logWarn("RUNNING CONFIGURE WITH ${commandLine.joinToString(" ")} CFLAGS: ${environment["CFLAGS"]}")
      }
      onlyIf {
        !makeFile.exists() && buildRequired.get().invoke(target)
      }

      val args = mutableListOf(
        "./Configure",
        target.opensslPlatform,
        "no-tests",
        "threads",
        "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}",
        "--libdir=lib",
      )

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


    KonanTarget.MACOS_X64 -> "darwin64-x86_64-cc"
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