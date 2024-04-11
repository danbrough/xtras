package org.danbrough.ssh2

class SSH: JNIObject() {


  external override fun nativeInit(): Long

  external fun test(ref:Long)

  override fun close() {
    log.trace { "SSH::close()" }
  }

}