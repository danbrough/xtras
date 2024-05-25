package org.danbrough.jwt

import kotlin.test.Test

class Thang {
	companion object {
		operator fun invoke() {

		}
	}
}

class ThangContext

fun thang(block: ThangContext.() -> Unit) {

}

class JvmTests {
	@Test
	fun test1() {
		log.info { "test1()" }


	}
}