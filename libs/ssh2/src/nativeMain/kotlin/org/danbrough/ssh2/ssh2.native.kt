package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import org.danbrough.ssh2.cinterops.libssh2_init
import platform.android.JNIEnvVar
import platform.android.jclass
import platform.android.jint

private fun init() {
  Platform.isMemoryLeakCheckerActive = false
}

@Suppress("ClassName")
object SSH2_JNI_Impl {
  private const val JNI_PREFIX = "Java_org_danbrough_ssh2_SSH2JNI"

  @CName("${JNI_PREFIX}_initSSH2")
  fun initSSH2(env: CPointer<JNIEnvVar>, clazz: jclass, flags: jint): jint {
    init()
    log.info { "initSSH2()" }
    val rc = libssh2_init(flags)
    log.debug { "got rc: $rc" }
    return rc
  }
}
