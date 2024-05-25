package org.danbrough.jwt

actual object JWTScope

@OptIn(ExperimentalUnsignedTypes::class)
actual fun <R> JWTScope.decode(
	token: String,
	alg: JwtAlg,
	secret: UByteArray,
	block: JWTDecode.() -> R
): R {
	TODO("Not yet implemented")
}

actual fun <R> jwt(block: JWTScope.() -> R): R = JWTScope.block()