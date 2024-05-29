package org.danbrough.ssh2

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped


actual fun createSSH(): SSH = SSHNative()


actual fun <R> ssh(
	block: SSHScope.() -> R
): R = memScoped {
	RootScope.sshScope(block) {
		SSHScope(this@memScoped)
	}
}

fun <R> SSHScope.session(block: SSHSession.() -> R): R = sshScope(block, ::SSHSession)