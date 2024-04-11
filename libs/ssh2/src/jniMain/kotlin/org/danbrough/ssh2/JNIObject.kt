package org.danbrough.ssh2

abstract class JNIObject : JNISupport(),AutoCloseable {

  @Suppress("LeakingThis")
  private var nativeRef: Long = nativeCreate()

  abstract fun nativeCreate(): Long

  abstract fun nativeDestroy(ref:Long)

  override fun close() {
    if (nativeRef != 0L) {
      nativeDestroy(nativeRef)
      nativeRef = 0L
    }
  }

  protected fun finalize(){
    close()
  }
}