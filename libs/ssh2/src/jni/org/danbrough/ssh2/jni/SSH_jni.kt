@file:Suppress("FunctionName", "UNUSED_PARAMETER")

package org.danbrough.ssh2.jni

import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.toCPointer
import org.danbrough.ssh2.SSHNative
import org.danbrough.ssh2.log
import platform.android.JNIEnvVar
import platform.android.jclass
import platform.android.jlong
import platform.android.jobject

private const val JNI_PREFIX = "Java_org_danbrough_ssh2_SSHJni"


@CName("${JNI_PREFIX}_nativeCreate")
fun sshCreate(env: CPointer<JNIEnvVar>, clazz: jclass, obj: jobject): jlong {
  jniInit()
  log.trace { "__nativeInitSSH::nativeInit()" }
  return StableRef.create(SSHNative()).asCPointer().rawValue.toLong()
}


@CName("${JNI_PREFIX}_nativeDestroy")
fun sshDestroy(env: CPointer<JNIEnvVar>, clazz: jclass, ref: jlong) {
  jniInit()
  val sshRef = ref.toCPointer<COpaquePointerVar>()?.asStableRef<SSHNative>()
  log.trace { "__nativeDestroySSH:: destroying: $sshRef " }
  sshRef?.also {
    it.get().close()
    it.dispose()
  }
}

