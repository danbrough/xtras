package org.danbrough.ssh2


class SSHJni : JNIObject(), SSH {

	external override fun nativeCreate(): Long

	external override fun nativeDestroy(ref: Long)

	override fun close() {
	}
}

actual fun createSSH(): SSH = SSHJni()


actual open class BaseScope {
	actual fun release() {
	}

}


actual class RootScope


actual class SSHScope : Scope()

actual fun <R> ssh(block: SSHScope.() -> R): R {
	TODO("Not yet implemented")
}