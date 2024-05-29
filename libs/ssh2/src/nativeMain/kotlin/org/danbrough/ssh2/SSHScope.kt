package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValues
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.NativePlacement
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init

@Suppress("MemberVisibilityCanBePrivate")
actual class SSHScope(private val mScope: MemScope) : Scope, NativePlacement by mScope {

	init {
		libssh2_init(0).also {
			if (it != 0) error("libssh2_init() -> $it") else log.trace { "libssh2_init()" }
		}
	}

	val <T : CVariable> CValues<T>.ptr: CPointer<T>
		get() = this@ptr.getPointer(mScope)

	override fun release() {
		log.trace { "SSHScope::release()" }
		libssh2_exit()
	}
}