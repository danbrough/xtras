package org.danbrough.jwt


actual class JWTEncode

actual fun <R> jwtEncode(block: JWTEncode.() -> R): R {
	TODO("Not yet implemented")
}

actual class JWTDecode

@OptIn(ExperimentalUnsignedTypes::class)
actual fun <R> jwtDecode(token: String, alg: JwtAlg, secret:UByteArray, block: JWTDecode.() -> R): R {
	TODO("Not yet implemented")
}