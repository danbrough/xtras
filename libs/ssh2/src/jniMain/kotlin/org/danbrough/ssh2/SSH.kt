package org.danbrough.ssh2

class SSH(loadLibrary: () -> Unit = { SSH.loadLibrary() }) {

  companion object {
    fun loadLibrary() {
      System.loadLibrary("ssh2")
    }
  }
}