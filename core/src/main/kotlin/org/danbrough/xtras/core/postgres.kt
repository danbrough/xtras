package org.danbrough.xtras.core


import org.danbrough.xtras.ENV_BUILD_DIR
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.registerXtrasGitLibrary
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


fun Project.postgres(extnName: String = "postgres", block: XtrasLibrary.() -> Unit) =
	registerXtrasGitLibrary<XtrasLibrary>(extnName) {
		cinterops {
			declaration = """
    linkerOpts = -lpq
    
    """.trimIndent()
		}

		environment { target ->
			if (target != null) {
				if (target.family == Family.ANDROID) {
					environmentNDK(xtras, target, this@postgres)
				} else if (target == KonanTarget.LINUX_ARM64 || target == KonanTarget.LINUX_X64) {// || ((target == KonanTarget.MINGW_X64) && HostManager.hostIsMingw)) {
					environmentKonan(this@registerXtrasGitLibrary, target, this@postgres)
				}
			}
		}


		buildCommand {target->
			writer.println("""
				[ ! -f GNUmakefile ] && ./configure --host=${target.hostTriplet} --prefix=${'$'}${ENV_BUILD_DIR} \
				--without-readline --without-icu --with-system-tzdata=/usr/sbin/zic 
			""".trimIndent())



			if (target == KonanTarget.MINGW_X64){
				writer.println("ln -s ./src/timezone/zic.exe  ./src/timezone/zic")
			}

			writer.println("""
				make
				make install
			""".trimIndent())

		}


		block()
	}
