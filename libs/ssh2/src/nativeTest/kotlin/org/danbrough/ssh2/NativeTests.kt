package org.danbrough.ssh2

import kotlin.test.Test

class NativeTests {
	@Test
	fun test1() {
		log.info { "test1()" }
		ssh {
			log.trace { "running in $this " }

			session {
				log.trace { "running in $this" }

				connect("sally", "127.0.0.1", 2222)
			}
		}
	}
}