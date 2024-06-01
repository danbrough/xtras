package org.danbrough.ssh2


class SSHJni : JNIObject(), SSH {

	external override fun nativeCreate(): Long

	external override fun nativeDestroy(ref: Long)

	override fun close() {
	}
}

actual fun createSSH(): SSH = SSHJni()




actual suspend fun <R> ssh(block: suspend SSHScope.() -> R): R {
	TODO("Not yet implemented")
}

actual class SSHScope : Scope {
	override fun close() {
		TODO("Not yet implemented")
	}
}

actual interface Scope {
	actual fun close()
}

actual class SSHSession