package org.danbrough.jwt

import org.danbrough.jwt.cinterops.JWT_ALG_ES256
import org.danbrough.jwt.cinterops.JWT_ALG_ES384
import org.danbrough.jwt.cinterops.JWT_ALG_ES512
import org.danbrough.jwt.cinterops.JWT_ALG_HS256
import org.danbrough.jwt.cinterops.JWT_ALG_HS384
import org.danbrough.jwt.cinterops.JWT_ALG_HS512
import org.danbrough.jwt.cinterops.JWT_ALG_NONE
import org.danbrough.jwt.cinterops.JWT_ALG_PS256
import org.danbrough.jwt.cinterops.JWT_ALG_PS384
import org.danbrough.jwt.cinterops.JWT_ALG_PS512
import org.danbrough.jwt.cinterops.JWT_ALG_RS256
import org.danbrough.jwt.cinterops.JWT_ALG_RS384
import org.danbrough.jwt.cinterops.JWT_ALG_RS512
import org.danbrough.jwt.cinterops.JWT_ALG_TERM
import org.danbrough.jwt.cinterops.jwt_alg


import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped

val JwtAlg.algorithm: jwt_alg
	get() = when (this) {
		JwtAlg.NONE -> JWT_ALG_NONE
		JwtAlg.HS256 -> JWT_ALG_HS256
		JwtAlg.HS384 -> JWT_ALG_HS384
		JwtAlg.HS512 -> JWT_ALG_HS512
		JwtAlg.RS256 -> JWT_ALG_RS256
		JwtAlg.RS384 -> JWT_ALG_RS384
		JwtAlg.RS512 -> JWT_ALG_RS512
		JwtAlg.ES256 -> JWT_ALG_ES256
		JwtAlg.ES384 -> JWT_ALG_ES384
		JwtAlg.ES512 -> JWT_ALG_ES512
		JwtAlg.PS256 -> JWT_ALG_PS256
		JwtAlg.PS384 -> JWT_ALG_PS384
		JwtAlg.PS512 -> JWT_ALG_PS512
		JwtAlg.TERM -> JWT_ALG_TERM
	}


internal fun <R, S : JWT> jwtScope(block: S.() -> R, creator: (MemScope) -> S): R = memScoped {
	val jwt = creator(this)
	runCatching {
		jwt.block()
	}.also { jwt.release() }.getOrThrow()
}


actual fun <R> JWTScope.encode(block: JWTEncode.() -> R): R = jwtScope(block, ::JWTEncode)

actual fun <R> JWTScope.decode(
	token: String,
	alg: JwtAlg,
	secret: UByteArray,
	block: JWTDecode.() -> R
): R = jwtScope(block) {
	JWTDecode(it, token, alg, secret)
}


actual class JWTScope(val memScope: MemScope)

actual fun <R> jwt(block: JWTScope.() -> R): R = memScoped {
	JWTScope(this).block()
}