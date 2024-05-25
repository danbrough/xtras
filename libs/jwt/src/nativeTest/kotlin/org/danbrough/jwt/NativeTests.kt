package org.danbrough.jwt

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.datetime.Clock
import org.danbrough.jwt.cinterops.JWT_ALG_HS512
import org.danbrough.jwt.cinterops.jwt_add_grant
import org.danbrough.jwt.cinterops.jwt_add_grant_bool
import org.danbrough.jwt.cinterops.jwt_add_grant_int
import org.danbrough.jwt.cinterops.jwt_decode
import org.danbrough.jwt.cinterops.jwt_dump_fp
import org.danbrough.jwt.cinterops.jwt_encode_str
import org.danbrough.jwt.cinterops.jwt_free
import org.danbrough.jwt.cinterops.jwt_free_str
import org.danbrough.jwt.cinterops.jwt_new
import org.danbrough.jwt.cinterops.jwt_set_alg
import org.danbrough.jwt.cinterops.jwt_t
import org.danbrough.jwt.cinterops.jwt_valid_free
import org.danbrough.jwt.cinterops.jwt_valid_new
import org.danbrough.jwt.cinterops.jwt_valid_t
import org.danbrough.jwt.cinterops.jwt_validate
import platform.posix.fflush
import platform.posix.stdout
import kotlin.test.Test

val secret_512 =
	"Cpl2CkWaVuoXCTdPukDlG72fzhBFSIoar0Q/aMmjehWpaCY5/5Fl5neG+92eX9Jh2B/kJkie0*3JCrpJd9VVMbEbF9mDvuhpOuylOTITlxjOhkY+s3rGC4UORjzWkWSrOF4fzdn/929eYQ/Im4xoHMt3mAGRlRgMtTHmpEhvYVRQLahV4MJrdfjd1hFixlYMsFnaEfhD19LW15Jlu3c+I+81HKbN/90nn/QlWsAP650eSkUY6Tci3UwuzLg9kW7POKrBicwHsvKcAuvoBp0xkLYoq9oprT/nH7EphwUwzxPkvYm+Vp3nZdPWnScddSE79O4jOKrMPkuSpV0/Liq/y+iugYajC8VHf3FoRIotr3xh4u4Ci9Naljseck9QQDC2d9ztMScFZVOINIYEHx3O2QFGej5o9+XBbc68XUgaVVm79r3l8TqKnlyapM79cmr9xrNyj8TdnqPobsdQlyNQ2iqHtk/4LVAAEXAuj2ri1tyl5twSrbzXqE1O0zNFT/j1".encodeToByteArray()
		.toUByteArray()


class NativeTests {

	private fun generateToken(): String {

		val expectedToken =
			"eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY3RpdmUiOnRydWUsImV4cCI6MTIzNDU2NzkwLCJpYXQiOjEyMzQ1Njc4OSwibWVzc2FnZSI6IkhlbGxvIFdvcmxkIn0.7byMy60JHC9G3t7dnAXvPM8Q3YAw0mE987-OVJ4dVs3lAhP9PzzpxqbegAFBy3QRcq55nXF5_mQb6-QfpmA9iQ"

		memScoped {
			val pJwt: CPointerVar<jwt_t> = alloc()
			jwt_new(pJwt.ptr).also {
				if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_new success" }
			}
			val jwt: CPointer<jwt_t> = pJwt.value ?: error("pJwt.value is null")

			jwt_add_grant(jwt, "message", "Hello World")
			jwt_add_grant_bool(jwt, "active", 1)

			jwt_add_grant_int(jwt, "iat", 123456789L).also {
				if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_add_grant_int success" }
			}

			jwt_add_grant_int(jwt, "exp", 123456789L + 1).also {
				if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_add_grant_int success" }
			}


			jwt_set_alg(
				jwt,
				JWT_ALG_HS512,
				secret_512.toCValues(),
				secret_512.size
			).also {
				if (it != 0) error("jwt_set_alg returned $it") else log.trace { "jwt_set_alg success" }
			}

			log.trace { "jwt_dump_fp(jwt, stdout, 1) ->" }
			jwt_dump_fp(jwt, stdout, 1)
			fflush(stdout)
			log.trace { "end of jwt_dump_fp" }

			val out = jwt_encode_str(jwt) ?: error("jwt_encode_str returned null")
			val token = out.toKString()
			if (token != expectedToken)
				log.error { "expecting $expectedToken" }

			jwt_free_str(out)
			jwt_free(jwt)

			return token
		}
	}

	private fun printToken(tok: String) {
		memScoped {
			val pJwt: CPointerVar<jwt_t> = alloc()
			jwt_decode(pJwt.ptr, tok, secret_512.toCValues(), secret_512.size).also {
				if (it != 0) error("jwt_decode returned $it") else log.trace { "jwt_decode success" }
			}
			jwt_dump_fp(pJwt.value, stdout, 1)
			fflush(stdout)
			jwt_free(pJwt.value)
		}
	}

	private fun validateTest() {
		memScoped {
			val pJwt: CPointerVar<jwt_t> = alloc()
			jwt_new(pJwt.ptr).also {
				if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_new success" }
			}
			val jwt: CPointer<jwt_t> = pJwt.value ?: error("pJwt.value is null")

			jwt_set_alg(
				jwt,
				JWT_ALG_HS512,
				secret_512.toCValues(),
				secret_512.size
			).also {
				if (it != 0) error("jwt_set_alg returned $it") else log.trace { "jwt_set_alg success" }
			}

			val now = Clock.System.now().epochSeconds
			val exp = now + 60 * 10 //10 mins
			jwt_add_grant_int(
				jwt,
				"exp",
				exp
			).also { if (it != 0) error("jwt_add_grant_int(jwt,\"exp\",exp) => $it") }
			jwt_add_grant_bool(jwt, "admin", 1)

			val out = jwt_encode_str(jwt) ?: error("jwt_encode_str returned null")
			val token = out.toKString()
			log.debug { "token: $token" }
			printToken(token)
			jwt_free_str(out)

			val pJwtValid: CPointerVar<jwt_valid_t> = alloc()
			jwt_valid_new(pJwtValid.ptr, JWT_ALG_HS512).also {
				if (it != 0) error("jwt_valid_new(pJwtValid.ptr, JWT_ALG_HS512) => $it")
			}
			val jwtValid = pJwtValid.value

			val valid = jwt_validate(jwt, jwtValid)
			log.info { "jwt_validate = $valid" }

			jwt_valid_free(jwtValid)
			jwt_free(jwt)
		}
	}

	fun basicTest(){
		log.debug { "secret size: ${secret_512.size}" }
		val tok = generateToken()
		log.debug { "got token: $tok" }
		printToken(tok)
		validateTest()
	}

	@Test
	fun test1() {
		log.info { "test1()" }

		val alg = JwtAlg.HS512
		val secret = secret_512

		val token = jwtEncode {
			log.debug { "token1: ${token()}" }
			claim("admin",true)
			log.debug { "token2: ${token()}" }
			setAlgorithm(alg, secret)
			issuedAtNow()
			token()
		}

		log.debug { "final token: $token" }
		jwtDecode(token,alg, secret){
			printJson()
		}
	}
}