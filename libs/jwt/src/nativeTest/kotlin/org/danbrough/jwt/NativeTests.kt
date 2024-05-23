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
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import org.danbrough.jwt.cinterops.JWT_ALG_HS256
import org.danbrough.jwt.cinterops.jwt_add_grant_int
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
import kotlin.test.Test

const val secret = "secret"


class NativeTests {

  private fun generateToken(){
    val expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjEyMzQ1Njc4OX0.Mjhfeh2uL0OqFVsk3p6tQ4M8J8dmL5PBVBw5Yzlh3ZE"

    memScoped {
      val pJwt:CPointerVar<jwt_t> = alloc()
      jwt_new(pJwt.ptr).also {
        if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_new success" }
      }
      val jwt:CPointer<jwt_t> = pJwt.value ?: error("pJwt.value is null")

      jwt_add_grant_int(jwt,"iat",123456789L).also {
        if (it != 0) error("jwt_add_grant_int returned $it") else log.trace { "jwt_add_grant_int success" }
      }

      jwt_set_alg(jwt, JWT_ALG_HS256,secret.cstr.ptr.reinterpret(), strlen(secret).convert()).also {
        if (it != 0) error("jwt_set_alg returned $it") else log.trace { "jwt_set_alg success" }
      };

      log.trace { "jwt_dump_fp(jwt, stdout, 1) ->" }
      jwt_dump_fp(jwt, stdout, 1)
      fflush(stdout)
      log.trace { "end of jwt_dump_fp" }

      val out = jwt_encode_str(jwt) ?: error("jwt_encode_str returned null")
      val token = out.toKString()
      log.info { token }
      if (token != expectedToken)
        log.error { "expecting $expectedToken" }

      jwt_free_str(out)
      jwt_free(jwt)

      log.info { "done" }
    }
  }
  @Test
  fun test1() {
    log.info { "test1()" }
    repeat(10_000){
      log.warn { "generate token: $it" }
      generateToken()
    }

  }
}