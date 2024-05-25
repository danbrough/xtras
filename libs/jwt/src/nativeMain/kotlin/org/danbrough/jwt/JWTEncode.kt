package org.danbrough.jwt

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.danbrough.jwt.cinterops.jwt_add_grant
import org.danbrough.jwt.cinterops.jwt_add_grant_bool
import org.danbrough.jwt.cinterops.jwt_add_grant_int
import org.danbrough.jwt.cinterops.jwt_encode_str
import org.danbrough.jwt.cinterops.jwt_free
import org.danbrough.jwt.cinterops.jwt_free_str
import org.danbrough.jwt.cinterops.jwt_new
import org.danbrough.jwt.cinterops.jwt_set_alg
import org.danbrough.jwt.cinterops.jwt_t
import kotlin.time.Duration

@Suppress("MemberVisibilityCanBePrivate")
actual class JWTEncode( mScope: MemScope) : JWT(mScope){

	override val jwt: CPointer<jwt_t> = memScoped {
		val pJwt: CPointerVar<jwt_t> = alloc()
		jwt_new(pJwt.ptr).also {
			if (it != 0) error("jwt_new() => $it")
		}
		pJwt.value!!
	}

	fun setAlgorithm(alg: JwtAlg, key: UByteArray) = apply {
		jwt_set_alg(jwt, alg.algorithm, key.toCValues(), key.size).also {
			if (it != 0) error("jwt_set_alg $alg => $it")
		}
	}

	fun token(): String =
		jwt_encode_str(jwt)!!.let { p ->
			p.toKString().also {
				jwt_free_str(p)
			}
		}

	fun claim(name: String, value:String)  {
		jwt_add_grant(jwt, name, value).also {
			if (it != 0) error("jwt_add_grant_bool => $it")
		}
	}

	fun claim(name: String, boolean: Boolean)  {
		jwt_add_grant_bool(jwt, name, if (boolean) 1 else 0).also {
			if (it != 0) error("jwt_add_grant_bool => $it")
		}
	}

	fun claim(name: String, n: Long) {
		jwt_add_grant_int(jwt, name, n).also {
			if (it != 0) error("jwt_add_grant_int => $it")
		}
	}

	fun claim(name: String, date: Instant) = claim(name, date.epochSeconds)

	fun issuedAtNow() = claim("iat",Clock.System.now())

	fun expiresAt(date:Long) = claim("exp",date)
	fun expiresAt(date:Instant) = expiresAt(date.epochSeconds)
	fun expiresIn(duration: Duration) = expiresAt(Clock.System.now().plus(duration))
	fun expiresIn(duration: Long) = expiresAt(Clock.System.now().epochSeconds + duration)
}