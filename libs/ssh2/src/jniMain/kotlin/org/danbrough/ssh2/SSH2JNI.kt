package org.danbrough.ssh2

import java.io.FileWriter

object SSH2JNI {


  external fun initSSH2(initFlags: Int = 0): Int


}

