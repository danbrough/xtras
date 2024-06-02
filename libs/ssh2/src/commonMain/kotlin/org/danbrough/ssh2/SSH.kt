package org.danbrough.ssh2

interface SSH : AutoCloseable

expect fun createSSH(): SSH

internal val log = klog.logger("SSH2")

expect interface Scope {
	fun close()
}

internal object RootScope : Scope{
	override fun close() {
	}
}

expect class SSHScope : Scope{
	override fun close()
}

expect suspend fun <R> ssh(block: suspend SSHScope.() -> R): R

suspend fun <B : Scope, R, S : Scope> B.sshScope(block:suspend S.() -> R, creator: (B) -> S): R =
	creator(this).let { scope ->
		runCatching {
			scope.block()
		}.also { scope.close() }.getOrThrow()
	}

expect class SSHSession