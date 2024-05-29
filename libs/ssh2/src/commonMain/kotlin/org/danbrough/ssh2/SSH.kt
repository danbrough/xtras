package org.danbrough.ssh2

interface SSH : AutoCloseable

expect fun createSSH(): SSH

internal val log = klog.logger("SSH2")

interface Scope {
	fun release() {}
}

internal object RootScope : Scope

expect class SSHScope : Scope

expect fun <R> ssh(block: SSHScope.() -> R): R

fun <B : Scope, R, S : Scope> B.sshScope(block: S.() -> R, creator: (B) -> S): R =
	creator(this).let { scope ->
		runCatching {
			scope.block()
		}.also { scope.release() }.getOrThrow()
	}

