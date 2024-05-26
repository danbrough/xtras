package org.danbrough.jwt

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import org.danbrough.jwt.cinterops.jwt_dump_fp
import org.danbrough.jwt.cinterops.jwt_free
import org.danbrough.jwt.cinterops.jwt_t
import platform.posix._IO_FILE
import platform.posix.fflush
import platform.posix.stdout


abstract class JWT(private val mScope: MemScope) {
	abstract val jwt: CPointer<jwt_t>
	fun <R> memScoped(block: MemScope.() -> R): R = mScope.block()
	open fun release() {
		log.trace { "$this::release()" }
		jwt_free(jwt)
	}

	fun printJson(stream: CPointer<_IO_FILE>? = stdout) {
		jwt_dump_fp(jwt, stream, 1)
		fflush(stream)
	}
}