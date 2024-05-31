package org.danbrough.ssh2

import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.danbrough.xtras.support.getEnv
import kotlin.test.Test

class NativeTests {
	@Test
	fun test1() {
		log.info { "test1()" }
		val sallysPrivateKeyPath = getEnv("SSH_PRIVATE_KEY") ?: error("\$SSH_PRIVATE_KEY not set")
		//val sallysPrivateKey = SystemFileSystem.source(Path(sallysPrivateKeyPath)).buffered().readString()
		val sallysKeyPassphrase = "password"
		log.trace { "sallysPrivateKeyPath: $sallysPrivateKeyPath" }

		runBlocking {
			ssh {
				log.trace { "running in $this " }

				session {
					log.trace { "running in $this" }
					connect("127.0.0.1", 2222)
					authenticate("sally", privateKeyPath = sallysPrivateKeyPath, password = "password")

					channel {
						log.trace { "running in $this" }


						exec("date")
						readLoop()
					}

				}
			}
		}
	}
}