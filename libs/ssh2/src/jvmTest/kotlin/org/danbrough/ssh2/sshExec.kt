package org.danbrough.ssh2

fun sshExec() {
  log.info { "sshExec()" }
  val ssh = SSH()
  val ref = ssh.nativeInit()
  log.info { "ref returned as $ref" }

  JavaTest().counter = 22

  Dude().thang(ref)


}
