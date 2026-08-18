package org.danbrough.ssh2

abstract class JNISupport {

  companion object {
    init {
      log.trace { "JNISupport::loading library xtras_ssh2" }
      System.loadLibrary("xtras_ssh2")
    }
  }
}