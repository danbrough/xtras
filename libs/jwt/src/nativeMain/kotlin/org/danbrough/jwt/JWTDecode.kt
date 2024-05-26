package org.danbrough.jwt

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.value
import org.danbrough.jwt.cinterops.jwt_decode
import org.danbrough.jwt.cinterops.jwt_t

@Suppress("MemberVisibilityCanBePrivate")
actual class JWTDecode(mScope: MemScope, val token: String,val jwtAlg: JwtAlg,val secret: ByteArray) : JWT(mScope) {

	override val jwt: CPointer<jwt_t> = memScoped {
		val pJwt: CPointerVar<jwt_t> = alloc()
		val cSecret = secret.toUByteArray().toCValues()
		jwt_decode(pJwt.ptr, token, cSecret, cSecret.size).also {
			if (it != 0) error("jwt_decode => $it")
		}
		pJwt.value!!
	}

}
