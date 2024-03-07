@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras


import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

const val binaryPropertyPrefix = "xtras.bin"


data class Binaries(

  @XtraDSL
  var git: String = "git",

  @XtraDSL
  var wget: String = "wget",

  @XtraDSL
  var tar: String = "tar",

  @XtraDSL
  var autoreconf: String = "autoreconf",

  @XtraDSL
  var make: String = "make",

  @XtraDSL
  var cmake: String = "cmake",

  @XtraDSL
  var go: String = "go",

  @XtraDSL
  var bash: String = "bash",

  @XtraDSL
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

  @XtraDSL

  var basePath: List<String> =
    listOf("/bin", "/sbin", "/usr/bin", "/usr/sbin", "/usr/local/bin", "/opt/local/bin")

  @XtraDSL

  var androidNdkApiVersion = 21

//
//  @XtrasDSL

//  var shellPath: File.() -> String = { absolutePath.replace('\\', '/') }

  /**
   * The java language version to apply to jvm and kotlin-jvm builds.
   * Not applied if null.
   * default: 11
   */
  @XtraDSL

  var javaLanguageVersion: Int? = 11

  @XtraDSL

  var defaultEnvironment: Map<String, String> = buildMap {

    if (!HostManager.hostIsMingw) put("PATH", basePath.joinToString(File.pathSeparator))
    else put("BASH_ENV", "/etc/profile")

    put("MAKE", "make -j${Runtime.getRuntime().availableProcessors()}")

    //put("CFLAGS", "-O3 -pthread -Wno-macro-redefined -Wno-deprecated-declarations")

    put("KONAN_BUILD", "1")
  }


  @XtraDSL

  var androidNdkDir: File = ANDROID_NDK_NOT_SET


  @XtraDSL

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
        if (HostManager.hostIsLinux) clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_ARM32_HFP -> {
        if (HostManager.hostIsLinux) clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2  --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot"
      }

      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64, KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_ARM64, KonanTarget.IOS_X64, KonanTarget.IOS_ARM64 -> {
        //put("CC", "clang")
        //put("CXX", "g++")
        //put("LD", "lld")
      }

      KonanTarget.MINGW_X64 -> {

//        if (HostManager.hostIsLinux) clangArgs =
//          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/msys2-mingw-w64-x86_64-2  " +
//              " --sysroot=$konanDir/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32"

//        put("CC", "x86_64-w64-mingw32-gcc")
//
//        if (HostManager.hostIsMingw) clangArgs = "--target=${target.hostTriplet} --gcc-toolchain=${
//          konanDir.resolve("dependencies/msys2-mingw-w64-x86_64-2").cygpath
//        }   --sysroot=${konanDir.resolve("dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32").cygpath}"

        //clangArgs = "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"

        put("CC", "x86_64-w64-mingw32-gcc")
        //put("AR", "x86_64-w64-mingw32-ar")
        //put("RANLIB", "x86_64-w64-mingw32-ranlib")
        put("RC", "x86_64-w64-mingw32-windres")

        /*
             environment(
          "RC",
          //  buildEnvironment.konanDir.resolve("dependencies/msys2-mingw-w64-x86_64-2/bin/windres.exe")
          "/usr/bin/x86_64-w64-mingw32-windres"
        )
         */
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
          "${androidNdkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin").cygpath}${File.pathSeparator}${
            get("PATH")
          }"
        )


        //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
        put("CC", "${target.hostTriplet}${androidNdkApiVersion}-clang")
        put("CXX", "${target.hostTriplet}${androidNdkApiVersion}-clang++")
        put("AR", "llvm-ar")
        put("RANLIB", "ranlib")

      }

      else -> error("Unhandled target: $target")
    }

    if (clangArgs != null) {
      put("CC", "clang $clangArgs")
      put("CXX", "clang++ $clangArgs")
    }
  }

  fun getEnvironment(target: KonanTarget? = null) = buildMap {
    putAll(defaultEnvironment)
    if (target != null) environmentForTarget(target)
  }

  @XtraDSL
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }

  internal fun initialize(project: Project) {

    konanDir = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
      System.getProperty("user.home"), ".konan"
    )

    if (androidNdkDir == ANDROID_NDK_NOT_SET) {
      val ndkRoot = System.getenv("ANDROID_NDK_ROOT") ?: System.getenv("ANDROID_NDK_HOME")
      if (ndkRoot != null) androidNdkDir = File(ndkRoot)
      else {
        androidNdkDir = project.xtrasNdkDir

        project.logWarn("Neither ANDROID_NDK_ROOT or ANDROID_NDK_HOME are set!")
      }
    }

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
  ): String =
    if (HostManager.hostIsMingw)
      Runtime.getRuntime()
        .exec(arrayOf(binaries.cygpath, CygpathMode.UNIX.arg, path)).inputStream.readAllBytes()
        .decodeToString().trim()
    else
      path


  fun cygpath(file: File) = cygpath(file.absolutePath)

  val File.cygpath: String
    get() = cygpath(this)

}

const val XTRAS_EXTN_BUILD_ENVIRONMENT = "xtrasBuildEnvironment"

fun Project.xtrasBuildEnvironment(configure: BuildEnvironment.() -> Unit = {}): BuildEnvironment =

  extensions.findByType<BuildEnvironment>()?.also {
    return (it.clone() as BuildEnvironment).also(configure)
  } ?: extensions.create<BuildEnvironment>(XTRAS_EXTN_BUILD_ENVIRONMENT).apply {
    initialize(this@xtrasBuildEnvironment)
    configure()
  }


fun File.cygpath(buildEnvironment: BuildEnvironment) = buildEnvironment.cygpath(this)



