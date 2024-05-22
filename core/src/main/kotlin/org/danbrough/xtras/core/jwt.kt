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


fun Project.jwt(ssl:XtrasLibrary, jansson:XtrasLibrary,extnName: String = "jwt", block: XtrasLibrary.() -> Unit) =
	registerXtrasGitLibrary<XtrasLibrary>(extnName) {
		cinterops {
			declaration = """
				headers = jwt.h
				linkerOpts = -ljwt -ljansson
    
    """.trimIndent()
		}

		environment { target ->
			if (target != null) {
				if (target.family == Family.ANDROID) {
					environmentNDK(xtras, target, this@jwt)
				} else if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.LINUX_X64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
					environmentKonan(this@registerXtrasGitLibrary, target, this@jwt)
				}

				put("JANSSON_CFLAGS","-I${jansson.libsDir(target).resolve("include")}")
				put("JANSSON_LDFLAGS","-ljansson -L${jansson.libsDir(target).resolve("lib")}")

				put("OPENSSL_CFLAGS","-I${ssl.libsDir(target).resolve("include")}")
				put("OPENSSL_LDFLAGS","-lssl -lcrypt -L${ssl.libsDir(target).resolve("lib")}")
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
