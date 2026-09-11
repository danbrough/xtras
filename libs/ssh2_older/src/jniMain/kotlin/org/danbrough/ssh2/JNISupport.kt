package org.danbrough.ssh2

open class JNISupport {

  companion object {
    init {
      log.trace { "JNISupport::loading library xtras_ssh2" }
      System.loadLibrary("xtras_ssh2")
    }
  }
}