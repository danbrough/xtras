package org.danbrough.jwt

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.value
import org.danbrough.jwt.cinterops.jwt_dump_fp
import org.danbrough.jwt.cinterops.jwt_free
import org.danbrough.jwt.cinterops.jwt_t
import platform.posix._IO_FILE
import platform.posix.fflush
import platform.posix.free
import platform.posix.stdout


abstract class MemScopePointer<T: CPointed>(private val mScope: MemScope, private val dispose: CPointer<T>.()->Unit = {free(this)}) {
	abstract val ptr: CPointer<T>
	fun <R> memScoped(block: MemScope.() -> R): R = mScope.block()
}

abstract class JWT(private val mScope: MemScope) {
	abstract val jwt: CPointer<jwt_t>
	fun <R> memScoped(block: MemScope.() -> R): R = mScope.block()
	open fun release() {
		log.error { "release()" }
		jwt_free(jwt)
	}

	fun printJson(stream: CPointer<_IO_FILE>? = stdout) {
		jwt_dump_fp(jwt, stream, 1)
		fflush(stream)
	}
}