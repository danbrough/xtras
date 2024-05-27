package org.danbrough.ssh2


class SSHJni : JNIObject(), SSH {

  external override fun nativeCreate(): Long

  external override fun nativeDestroy(ref: Long)

  override fun close() {
  }
}

actual fun createSSH(): SSH = SSHJni()
actual class SshSocket