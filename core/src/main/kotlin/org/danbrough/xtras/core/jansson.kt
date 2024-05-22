package org.danbrough.xtras.core


import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.registerXtrasGitLibrary
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

fun Project.jansson(extnName: String = "jansson", block: XtrasLibrary.() -> Unit) =
	registerXtrasGitLibrary<XtrasLibrary>(extnName) {
		cinterops {
			declaration = """
				headers = jansson.h
				linkerOpts = -ljansson
    
    """.trimIndent()
		}

		environment { target ->
			if (target != null) {
				if (target.family == Family.ANDROID) {
					environmentNDK(xtras, target, this@jansson)
				} else if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.LINUX_X64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
					environmentKonan(this@registerXtrasGitLibrary, target, this@jansson)
				}

			}
		}

		buildCommand {target->
			writer.println("""
        [ ! -f configure ] && autoreconf -fi 
        [ ! -f Makefile ] && ./configure --host=${target.hostTriplet} --prefix=${buildDir(target).mixedPath}
        make
        make install
      """.trimIndent())
		}

		block()
	}
