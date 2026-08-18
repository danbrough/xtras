@file:Suppress("FunctionName", "UNUSED_PARAMETER")
@file:OptIn(ExperimentalForeignApi::class)

package org.danbrough.ssh2.jni

import kotlinx.cinterop.ExperimentalForeignApi

inline fun jniInit() {
  Platform.isMemoryLeakCheckerActive = false
}

