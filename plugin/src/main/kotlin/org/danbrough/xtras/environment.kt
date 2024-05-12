@file:Suppress("SpellCheckingInspection")

package org.danbrough.xtras

import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

typealias XtrasEnvironment = MutableMap<String, Any>
typealias XtrasEnvironmentConfig = XtrasEnvironment.(target: KonanTarget?) -> Unit

val XtrasExtension.INITIAL_ENVIRONMENT: XtrasEnvironmentConfig
	get() = { target ->
		//project.logTrace("INITIAL_ENVIRONMENT: target: $target")

		put("HOME", project.unixPath(File(System.getProperty("user.home"))))


		put("MAKEFLAGS", "-j${Runtime.getRuntime().availableProcessors()}")

		if (HostManager.hostIsMingw) {
			put(
				"PATH", project.pathOf(
					project.xtrasMsysDir.resolveAll("mingw64", "bin"),
					project.xtrasMsysDir.resolveAll("usr", "local", "bin"),
					project.xtrasMsysDir.resolveAll("usr", "bin"),
					project.xtrasMsysDir.resolveAll("bin"),
					get("PATH")
				)
			)
		} else {
			put("PATH", project.pathOf("/bin", "/usr/bin", "/usr/local/bin", get("PATH")))
		}

		if (target != null) {
			if (target.family == Family.ANDROID)
				environmentNDK(this@INITIAL_ENVIRONMENT, target, project)
			else if (target.family.isAppleFamily)
				environmentApple(target)
		}
	}

private fun XtrasEnvironment.environmentApple(target: KonanTarget) {
	put(
		"CFLAGS",
		"-isysroot /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk"
	)
	val clangArgs =
		"--target=${target.hostTriplet}"
	put("CLANG_ARGS", clangArgs)
	put("CC", "clang $clangArgs")
	put("CXX", "clang++ $clangArgs")
}


fun XtrasEnvironment.environmentNDK(xtras: XtrasExtension, target: KonanTarget, project: Project) {
	put("ANDROID_NDK_ROOT", xtras.androidConfig.ndkDir)

	val archFolder = when {
		HostManager.hostIsLinux -> "linux-x86_64"
		HostManager.hostIsMac -> "darwin-x86_64"
		HostManager.hostIsMingw -> "windows-x86_64"
		else -> error("Unhandled host: ${HostManager.host}")
	}

	val ndkPath = project.pathOf(
		xtras.androidConfig.ndkDir.resolve("bin"),
		xtras.androidConfig.ndkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin"),
		get("PATH")
	)
	xtras.project.logTrace("environmentNDK: NDK_PATH: $ndkPath")
	put(
		"PATH",
		ndkPath
	)

	//basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
	put("PREFIX", "${target.hostTriplet}${xtras.androidConfig.ndkApiVersion}-")

	put("CC", "clang")
	put("CXX", "clang++")

	put("AR", "llvm-ar")
	put("RANLIB", "ranlib")

}

fun XtrasEnvironment.environmentKonan(library: XtrasLibrary, target: KonanTarget,project: Project) {
	//put("PATH",pathOf(project.xtrasKon))
	val depsDir = library.project.konanDir.resolve("dependencies")
	val llvmPrefix = if (HostManager.hostIsLinux || HostManager.hostIsMingw) "llvm-" else "apple-llvm"
	val llvmDir = depsDir.listFiles()?.firstOrNull {
		it.isDirectory && it.name.startsWith(llvmPrefix)
	} ?: error("No directory beginning with \"llvm-\" found in ${depsDir.mixedPath}")
	put("PATH", project.pathOf(llvmDir.resolve("bin"), get("PATH")))
	val clangArgs =
		when (target) {
			KonanTarget.LINUX_ARM64 ->
				"--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")}" +
						" --sysroot=${
							depsDir.resolveAll(
								"aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2",
								"aarch64-unknown-linux-gnu",
								"sysroot"
							)
						}"

			KonanTarget.LINUX_X64 ->
				"--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")}" +
						" --sysroot=${
							depsDir.resolveAll(
								"x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2",
								"x86_64-unknown-linux-gnu",
								"sysroot"
							)
						}"


			KonanTarget.MINGW_X64 ->
				"--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("msys2-mingw-w64-x86_64-2")}" +
						" --sysroot=${
							depsDir.resolveAll(
								"msys2-mingw-w64-x86_64-2",
								"x86_64-w64-mingw32",
								"sysroot"
							)
						}"


			/*			KonanTarget.ANDROID_ARM64 ->
							"--target=${target.hostTriplet} --gcc-toolchain=${depsDir.resolve("target-toolchain-2-linux-android_ndk")}" +
									" --sysroot=${
										depsDir.resolveAll(
											"target-toolchain-2-linux-android_ndk",
											"aarch64-linux-android",
										)
									}"*/


			else -> error("Unsupported konan target: $target")
		}
	put("CLANG_ARGS", clangArgs)
	put("CC", "clang $clangArgs")
	put("CXX", "clang++ $clangArgs")

}

