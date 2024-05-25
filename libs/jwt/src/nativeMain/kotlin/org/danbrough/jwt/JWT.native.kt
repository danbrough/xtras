package org.danbrough.jwt

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped


fun <R,S:JWT> jwtScope(block: S.() -> R,creator:(MemScope)->S): R = memScoped {
	val jwt = creator(this)
	runCatching {
		jwt.block()
	}.also { jwt.release() }.getOrThrow()
}


actual fun <R> jwtEncode(block: JWTEncode.() -> R): R = jwtScope(block,::JWTEncode)


actual fun <R> jwtDecode(token: String,alg: JwtAlg,secret:UByteArray, block: JWTDecode.() -> R): R =
	jwtScope(block){
		JWTDecode(it,token,alg,secret)
	}
	