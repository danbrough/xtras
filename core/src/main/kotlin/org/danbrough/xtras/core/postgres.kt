package org.danbrough.xtras.core


import org.danbrough.xtras.ENV_BUILD_DIR
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.environmentKonan
import org.danbrough.xtras.environmentNDK
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.kotlinTargetName
import org.danbrough.xtras.mixedPath
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.registerXtrasGitLibrary
import org.danbrough.xtras.tasks.SourceTaskName
import org.danbrough.xtras.tasks.compileSource
import org.danbrough.xtras.tasks.configureSource
import org.danbrough.xtras.tasks.installSource
import org.danbrough.xtras.tasks.prepareSource
import org.danbrough.xtras.xtrasLibsDir
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
/*
		configureSource(dependsOn = SourceTaskName.EXTRACT) { target ->
			outputs.file(workingDir.resolve("Makefile"))

			val args = mutableListOf(
				"sh",
				"./configure"
			)


			args += "--host=${target.hostTriplet}"
			//args += "--target=${target.hostTriplet}"

			args += listOf(
				"--prefix=${buildDir(target).mixedPath}",
				"--exec-prefix=${environment["PREFIX"]}",
				"--without-readline",
				"--without-icu",
			)

			args += "--with-libssl-prefix=${xtrasLibsDir}/openssl/${project.projectProperty<String>("openssl.version")}/${target.kotlinTargetName}" //TODO fix this
			xtrasCommandLine(args)
		}
*/



		block()
	}