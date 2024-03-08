package org.danbrough.ssh2

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import org.danbrough.nativetests.message
import org.danbrough.nativetests.test1
import kotlin.test.Test

@OptIn(ExperimentalForeignApi::class)
class NativeTests {
  @Test
  fun test() {
    log.info { "test()" }
    message()

    val s = nativeHeap.alloc<Int>(512)
    test1(s.ptr)
    log.warn { "s is ${s.value}" }
    nativeHeap.free(s.rawPtr)

    memScoped {
      val n = alloc<IntVar>()
      n.value = 123

      test1(n.ptr)
      log.warn { "n is ${n.value}" }

    }
  }
}