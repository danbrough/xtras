@file:Suppress("FunctionName")
@file:OptIn(ExperimentalForeignApi::class)

package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import org.danbrough.ssh2.cinterops.libssh2_init
import platform.android.JNIEnvVar
import platform.android.jclass
import platform.android.jint

private fun init() {
  Platform.isMemoryLeakCheckerActive = false
}

private const val JNI_PREFIX = "Java_org_danbrough_ssh2_SSH2JNI"

@CName("${JNI_PREFIX}_initSSH2")
fun __initSSH2(env: CPointer<JNIEnvVar>, clazz: jclass, initFlags: jint): jint {
  init()
  return libssh2_init(initFlags).also {
    log.trace { "libssh2_init($initFlags) == $it" }
  }
}

