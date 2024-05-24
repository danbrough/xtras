package org.danbrough.jwt

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.io.buffered
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.source
import kotlinx.io.readString
import org.danbrough.jwt.cinterops.JWT_ALG_HS256
import org.danbrough.jwt.cinterops.jwt_add_grant
import org.danbrough.jwt.cinterops.jwt_add_grant_bool
import org.danbrough.jwt.cinterops.jwt_add_grant_int
import org.danbrough.jwt.cinterops.jwt_add_grants_json
import org.danbrough.jwt.cinterops.jwt_dump_fp
import org.danbrough.jwt.cinterops.jwt_encode_str
import org.danbrough.jwt.cinterops.jwt_free
import org.danbrough.jwt.cinterops.jwt_free_str
import org.danbrough.jwt.cinterops.jwt_malloc_tVar
import org.danbrough.jwt.cinterops.jwt_new
import org.danbrough.jwt.cinterops.jwt_set_alg
import org.danbrough.jwt.cinterops.jwt_t
import platform.posix.alloca
import platform.posix.fflush
import platform.posix.memchr
import platform.posix.stderr
import platform.posix.stderr_
import platform.posix.stdout
import platform.posix.strlen
import kotlin.random.Random
import kotlin.test.Test

val secret_512 =
	"Cpl2CkWaVuoXCTdPukDlG72fzhBFSIoar0Q/aMmjehWpaCY5/5Fl5neG+92eX9Jh2B/kJkie0*3JCrpJd9VVMbEbF9mDvuhpOuylOTITlxjOhkY+s3rGC4UORjzWkWSrOF4fzdn/929eYQ/Im4xoHMt3mAGRlRgMtTHmpEhvYVRQLahV4MJrdfjd1hFixlYMsFnaEfhD19LW15Jlu3c+I+81HKbN/90nn/QlWsAP650eSkUY6Tci3UwuzLg9kW7POKrBicwHsvKcAuvoBp0xkLYoq9oprT/nH7EphwUwzxPkvYm+Vp3nZdPWnScddSE79O4jOKrMPkuSpV0/Liq/y+iugYajC8VHf3FoRIotr3xh4u4Ci9Naljseck9QQDC2d9ztMScFZVOINIYEHx3O2QFGej5o9+XBbc68XUgaVVm79r3l8TqKnlyapM79cmr9xrNyj8TdnqPobsdQlyNQ2iqHtk/4LVAAEXAuj2ri1tyl5twSrbzXqE1O0zNFT/j1".encodeToByteArray()
		.toCValues()


class NativeTests {

	private fun generateToken():String {


		val expectedToken =
			"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY3RpdmUiOnRydWUsImlhdCI6MTIzNDU2Nzg5LCJtZXNzYWdlIjoiSGVsbG8gV29ybGQifQ.ARXranW0SJZFjqvM9tRCMCe-IQGWTwJgnKhEVDNG474"

		memScoped {
			val pJwt: CPointerVar<jwt_t> = alloc()
			jwt_new(pJwt.ptr).also {
				if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_new success" }
			}
			val jwt: CPointer<jwt_t> = pJwt.value ?: error("pJwt.value is null")

			jwt_add_grant(jwt,"message","Hello World")
			jwt_add_grant_bool(jwt,"active",1)

			jwt_add_grant_int(jwt, "iat", 123456789L).also {
				if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_add_grant_int success" }
			}


			jwt_set_alg(
				jwt,
				JWT_ALG_HS256,
				secret_512.ptr.reinterpret(),
				secret_512.size
			).also {
				if (it != 0) error("jwt_set_alg returned $it") else log.trace { "jwt_set_alg success" }
			};

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

	@Test
	fun test1() {
		log.info { "test1()" }


		log.debug { "secret size: ${secret_512.size}" }

		val tok = generateToken()
		log.debug { "got token: $tok" }

		/*		repeat(10_000) {
					log.warn { "generate token: $it" }
					generateToken()
				}*/

	}
}