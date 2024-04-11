@file:Suppress("FunctionName")

package org.danbrough.ssh2

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import platform.android.JNIEnvVar
import platform.android.jclass
import platform.android.jlong
import platform.android.jobject

private const val JNI_PREFIX = "Java_org_danbrough_ssh2_SSH"

var ssh:SSH? = null
@CName("${JNI_PREFIX}_nativeInit")
fun __nativeInitSSH(env: CPointer<JNIEnvVar>, clazz: jclass,obj: jobject): jlong {
  jniInit()
  val ref = StableRef.create(SSH().also {
    it.counter = 128
    ssh = it })
  println("GOT REF: $ref = cpointer: ${ref.asCPointer()}")
  val p = ref.asCPointer().rawValue.toLong()
  println("returning p = $p")
  return p
}

@CName("Java_org_danbrough_ssh2_Dude_thang")
fun thang(env: CPointer<JNIEnvVar>, clazz: jclass,obj: jobject,ref:jlong) {
  jniInit()
  println("thang got ref: $ref")
  val cpointer = ref.toCPointer<COpaquePointerVar>()
  println("thang: cpointer $cpointer")
  println("thang: stableRef: ${cpointer?.asStableRef<SSH>()}")
  println("thang: stableRef.get(): ${cpointer?.asStableRef<SSH>()?.get()}")

}

@CName("${JNI_PREFIX}_test")
fun __jni_SSH_test(env: CPointer<JNIEnvVar>, clazz: jclass,obj: jobject,ref:jlong) {
  jniInit()
  println("got ref: $ref")
  ref.toCPointer<COpaquePointerVar>()?.asStableRef<SSH>()?.also {
    println("GOT REF as $it contains ${it.get()}")
  }


/*
  println("got p: $p")
  val ssh = p?.get()
  println("SSH: $ssh")
*/

}

