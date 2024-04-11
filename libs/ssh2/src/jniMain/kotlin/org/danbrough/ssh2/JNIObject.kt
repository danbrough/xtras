package org.danbrough.ssh2

abstract class JNIObject : JNISupport(),AutoCloseable {

  @Suppress("LeakingThis")
  private var nativeRef: Long = nativeCreate()

  /**
   * Create native peer and return stable reference to it
   */
  abstract fun nativeCreate(): Long

  /**
   * Destroy the native peer
   */
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