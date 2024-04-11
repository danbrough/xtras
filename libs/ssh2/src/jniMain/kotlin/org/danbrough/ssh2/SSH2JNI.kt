package org.danbrough.ssh2

import java.io.FileWriter

object SSH2JNI : JNISupport(){


  external fun initSSH2(initFlags: Int = 0): Int



}

