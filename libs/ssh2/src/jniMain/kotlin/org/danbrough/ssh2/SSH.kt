package org.danbrough.ssh2

class SSH: JNIObject() {

  /**
   * Create native peer and return stable reference to it
   */
  external override fun nativeCreate(): Long

  /**
   * Destroy the native peer
   */
  external override fun nativeDestroy(ref: Long)
}