package org.danbrough.jwt

actual object JWTScope

actual fun <R> JWTScope.decode(
	token: String,
	alg: JwtAlg,
	secret: ByteArray,
	block: JWTDecode.() -> R
): R {
	TODO("Not yet implemented")
}

actual fun <R> jwt(block: JWTScope.() -> R): R = JWTScope.block()