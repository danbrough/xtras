@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras


import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val binaryPropertyPrefix = "xtras.bin"


data class Binaries(

  @XtrasDSL
  var git: String = "git",

  @XtrasDSL
  var wget: String = "wget",

  @XtrasDSL
  var tar: String = "tar",

  @XtrasDSL
  var autoreconf: String = "autoreconf",

  @XtrasDSL
  var make: String = "make",

  @XtrasDSL
  var cmake: String = "cmake",

  @XtrasDSL
  var go: String = "go",

  @XtrasDSL
  var bash: String = "bash",

  @XtrasDSL
  var cygpath: String = "cygpath",
)

val binaryProperty: Project.(String, String) -> String = { exe, defValue ->
  projectProperty("$binaryPropertyPrefix.$exe") { defValue }
}


open class BuildEnvironment : Cloneable {
  companion object {
    val ANDROID_NDK_NOT_SET = File(System.getProperty("java.io.tmpdir"), "android_ndk_not_set")
  }

  var binaries = Binaries()

  lateinit var konanDir: File

  public override fun clone(): Any = BuildEnvironment().also {
    it.binaries = binaries.copy()
    it.konanDir = konanDir
    it.basePath = basePath
    it.androidNdkApiVersion = androidNdkApiVersion
    it.defaultEnvironment = defaultEnvironment
    it.androidNdkDir = androidNdkDir
    it.environmentForTarget = environmentForTarget
  }

  @XtrasDSL
  var basePath: List<String> =
    listOf("/bin", "/sbin", "/usr/bin", "/usr/sbin", "/usr/local/bin", "/opt/local/bin")

  @XtrasDSL
  var androidNdkApiVersion = 21

//
//  @XtrasDSL

//  var shellPath: File.() -> String = { absolutePath.replace('\\', '/') }

  /**
   * The java language version to apply to jvm and kotlin-jvm builds.
   * Not applied if null.
   * default: 11
   */
  @XtrasDSL
  var javaLanguageVersion: Int? = 11

  @XtrasDSL
  var defaultEnvironment: Map<String, String> = buildMap {

    if (!HostManager.hostIsMingw) put("PATH", basePath.joinToString(File.pathSeparator))
    else put("BASH_ENV", "/etc/profile")

    put("MAKEFLAGS", "-j${Runtime.getRuntime().availableProcessors()}")


    put("KONAN_BUILD", "1")
  }


  @XtrasDSL
  lateinit var androidNdkDir: File


  @XtrasDSL
  var environmentForTarget: MutableMap<String, String>.(KonanTarget) -> Unit = { target ->

    if (!HostManager.hostIsMac || !target.family.isAppleFamily) {
      val llvmPrefix = if (HostManager.hostIsLinux) "llvm-" else "apple-llvm"
      konanDir.resolve("dependencies").listFiles()
        ?.firstOrNull { it.isDirectory && it.name.startsWith(llvmPrefix) }?.also {
          put("PATH", "${it.resolve("bin").absolutePath}:${get("PATH")}")
        }
    }

    var clangArgs: String? = null

    when (target) {
      KonanTarget.LINUX_ARM64 -> {
        clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_X64 -> {
        clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_ARM32_HFP -> {
        clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2  --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot"
      }

      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64 -> {
        put(
          "CFLAGS",
          "-isysroot /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk"
        )
        clangArgs =
          "--target=${target.hostTriplet}"
      }

      KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_ARM64, KonanTarget.IOS_X64, KonanTarget.IOS_ARM64 -> {
        //put("CC", "clang")
        //put("CXX", "g++")
        //put("LD", "lld")
      }

      KonanTarget.MINGW_X64 -> {


        //put("CC","gcc")
        //put("CC", "x86_64-w64-mingw32-gcc")
        //put("AR", "x86_64-w64-mingw32-ar")
        //put("RANLIB", "x86_64-w64-mingw32-ranlib")
        //put("RC", "x86_64-w64-mingw32-windres")


      }

      KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {
        //library.project.log("ADDING NDK TO PATH")
        val archFolder = when {
          HostManager.hostIsLinux -> "linux-x86_64"
          HostManager.hostIsMac -> "darwin-x86_64"
          HostManager.hostIsMingw -> "windows-x86_64"
          else -> error("Unhandled host: ${HostManager.host}")
        }


        put(
          "PATH",
          "${androidNdkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin").absolutePath}${File.pathSeparator}${
            get("PATH")
          }"
        )


        //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
        put("PREFIX", "${target.hostTriplet}${androidNdkApiVersion}-")
        put("CC", "clang")
        put("CXX", "clang++")
        put("AR", "llvm-ar")
        put("RANLIB", "ranlib")


      }

      else -> error("Unhandled target: $target")
    }

    if (clangArgs != null) {
      put("CLANG_ARGS", clangArgs)
      put("CC", "clang $clangArgs")
      put("CXX", "clang++ $clangArgs")
    }
  }

  fun getEnvironment(target: KonanTarget? = null) = buildMap {
    putAll(defaultEnvironment)
    if (target != null) environmentForTarget(target)
  }

  @XtrasDSL
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }

  internal fun initialize(project: Project) {

    konanDir = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
      System.getProperty("user.home"), ".konan"
    )

    androidNdkDir = project.xtrasNdkDir

    binaries.apply {
      git = project.binaryProperty("git", git)
      wget = project.binaryProperty("wget", wget)
      go = project.binaryProperty("go", go)
      tar = project.binaryProperty("tar", tar)
      make = project.binaryProperty("make", make)
      cmake = project.binaryProperty("cmake", cmake)
      bash = project.binaryProperty("bash", bash)
      cygpath = project.binaryProperty("cygpath", cygpath)
      autoreconf = project.binaryProperty("autoreconf", autoreconf)
    }

    project.tasks.register("xtrasConfig") {
      group = XTRAS_TASK_GROUP
      description = "Prints out the xtras configuration details"

      doFirst {
        println(
          """

                Binaries:
                  $binaryPropertyPrefix.git:            ${binaries.git}
                  $binaryPropertyPrefix.wget:           ${binaries.wget}
                  $binaryPropertyPrefix.tar:            ${binaries.tar}
                  $binaryPropertyPrefix.go:             ${binaries.go}
                  $binaryPropertyPrefix.autoreconf:     ${binaries.autoreconf}
                  $binaryPropertyPrefix.make:           ${binaries.make}
                  $binaryPropertyPrefix.cmake:          ${binaries.cmake}

                Paths:
                  ${XtrasPath.XTRAS.propertyName}:        ${project.xtrasDir}
                  ${XtrasPath.LIBS.propertyName}:       ${project.xtrasLibsDir}
                  ${XtrasPath.DOWNLOADS.propertyName}:  ${project.xtrasDownloadsDir}
                  ${XtrasPath.SOURCE.propertyName}:  ${project.xtrasSourceDir}
                  ${XtrasPath.PACKAGES.propertyName}:   ${project.xtrasPackagesDir}
                  ${XtrasPath.DOCS.propertyName}:       ${project.xtrasDocsDir}
                  ${XtrasPath.INTEROPS.propertyName}:   ${project.xtrasCInteropsDir}
                  ${XtrasPath.LOGS.propertyName}:       ${project.xtrasLogsDir}
                  ${XtrasPath.MAVEN.propertyName}:      ${project.xtrasMavenDir}
                  ${XtrasPath.NDK.propertyName}:        ${project.xtrasNdkDir}
                  
                  
                BuildEnvironment:
                  androidNdkApiVersion:     $androidNdkApiVersion
                  androidNdkDir:            $androidNdkDir (${XtrasPath.NDK.propertyName})
                  
                """.trimIndent()
        )
      }
    }
  }


  enum class CygpathMode(val arg: String) {
    WINDOWS("-w"), MIXED("-m"), UNIX("-u"), DOS("-d");
  }

  fun cygpath(
    path: String,
    mode: CygpathMode = CygpathMode.UNIX
  ): String =
    if (HostManager.hostIsMingw)
      Runtime.getRuntime()
        .exec(arrayOf(binaries.cygpath, mode.arg, path)).inputStream.readAllBytes()
        .decodeToString().trim()
    else
      path


  fun cygpath(file: File, mode: CygpathMode = CygpathMode.UNIX) = cygpath(file.absolutePath, mode)

  //fun File.cygpath(mode: CygpathMode = CygpathMode.UNIX): String = cygpath(this,mode)

}

/*
const val XTRAS_EXTN_BUILD_ENVIRONMENT = "xtrasBuildEnvironment"

fun Project.xtrasBuildEnvironment(): BuildEnvironment =

  extensions.findByType<BuildEnvironment>()?: extensions.create<BuildEnvironment>(XTRAS_EXTN_BUILD_ENVIRONMENT).apply {
    initialize(this@xtrasBuildEnvironment)
  }


*/



