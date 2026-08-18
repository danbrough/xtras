package org.danbrough.ssh2

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped


actual fun createSSH(): SSH = SSHNative()


actual suspend fun <R> ssh(
	block: suspend SSHScope.() -> R
): R = memScoped {
	RootScope.sshScope(block) {
		SSHScope(this@memScoped)
	}
}

suspend fun <R> SSHScope.session(block: suspend SSHSession.() -> R): R = sshScope(block, ::SSHSession)

actual interface Scope : AutoCloseable {
	actual override fun close()
}