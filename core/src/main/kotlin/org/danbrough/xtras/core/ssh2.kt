package org.danbrough.xtras.core

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.pathOf
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.xtrasLibsDir
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


fun Project.ssh2(ssl: XtrasLibrary, extnName: String = "ssh2", block: XtrasLibrary.() -> Unit) =
	registerXtrasGitLibrary<XtrasLibrary>(extnName) {
		cinterops {
			declaration = """
        headers = libssh2.h  libssh2_publickey.h  libssh2_sftp.h
        linkerOpts = -lssh2 
    """.trimIndent()
		}

		environment { target ->
			if (target != null) {
				if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.LINUX_X64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
					environmentKonan(this@registerXtrasGitLibrary, target, this@ssh2)
				} else if (target.family == Family.ANDROID) {
					environmentNDK(xtras, target, this@ssh2)
					put("CC", "${get("PREFIX")}clang")
					put("CXX", "${get("PREFIX")}clang++")
				} else if (target == KonanTarget.MACOS_ARM64 || target == KonanTarget.MACOS_X64) {
					/*
					CC=/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang
CFLAGS='-arch arm64 -pipe -no-cpp-precomp -isysroot $SDKROOT -mmacosx-version-min=12.0 -fembed-bitcode'
CLANG=/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang
CPP='/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang -E'
CPPFLAGS='-arch arm64 -pipe -no-cpp-precomp -isysroot $SDKROOT -mmacosx-version-min=12.0'
					 */
					put(
						"PATH",
						pathOf(
							"Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin",
							get("PATH")
						)
					)
					put("CLANG", "clang")
					put("CPP", "clang -E")
					put("SDKROOT","/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX14.2.sdk")
					put("CFLAGS","-arch ${if (target == KonanTarget.MACOS_ARM64) "arm64" else "x86_64"} -pipe -no-cpp-precomp -isysroot ${get("SDKROOT")}  -mmacosx-version-min=12.0 -fembed-bitcode")
					put("CPPFLAGS","-arch ${if (target == KonanTarget.MACOS_ARM64) "arm64" else "x86_64"} -pipe -no-cpp-precomp -isysroot ${get("SDKROOT")}  -mmacosx-version-min=12.0 -fembed-bitcode")

				}
			}
		}

		buildCommand { target ->
			val binDir = buildDir(target).resolve("bin")
			val copyExamples = if (target == KonanTarget.MINGW_X64)
				"""cp example/*.exe ${pathOf(binDir)}""".trimIndent()
			else """
        mkdir ${binDir.absolutePath}
        cp example/.libs/* ${pathOf(binDir)}/
        """
			writer.println(
				"""
        [ ! -f configure ] && autoreconf -fi 
        [ ! -f Makefile ] && ./configure --host=${target.hostTriplet} --prefix=${pathOf(buildDir(target))} \
        --disable-debug --disable-dependency-tracking --disable-silent-rules --with-libz \
        --with-libssl-prefix=${pathOf(xtrasLibsDir)}/openssl/${project.projectProperty<String>("openssl.version")}/${target.kotlinTargetName}       
        make
        make install
        $copyExamples
			""".trimIndent()
			)
		}

		block()
	}


