package org.danbrough.jwt

import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import org.danbrough.jwt.cinterops.jwt_add_grant_int
import org.danbrough.jwt.cinterops.jwt_malloc_tVar
import org.danbrough.jwt.cinterops.jwt_new
import org.danbrough.jwt.cinterops.jwt_t
import platform.posix.alloca
import platform.posix.memchr
import kotlin.test.Test

class NativeTests {
  @Test
  fun test1() {
    log.info { "test1()" }
    memScoped {
      val jwt = alloc<jwt_malloc_tVar>()
      jwt_new(jwt.ptr.reinterpret()).also {
        log.trace { "jwt_new returned $it" }
      }
      jwt_add_grant_int(jwt.ptr.reinterpret(),"iat",1L).also {
        log.trace { "jwt_add_grant_int returned $it" }
      }

    }
  }
}