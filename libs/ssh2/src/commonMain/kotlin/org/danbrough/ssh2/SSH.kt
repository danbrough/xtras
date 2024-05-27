package org.danbrough.ssh2

interface SSH : AutoCloseable

expect fun createSSH(): SSH

internal val log = klog.logger("SSH2")

