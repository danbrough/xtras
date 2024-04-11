package org.danbrough.ssh2

abstract class JNIObject : JNISupport(),AutoCloseable {

  private var nativeRef: Long = 0
  abstract fun nativeInit(): Long

}