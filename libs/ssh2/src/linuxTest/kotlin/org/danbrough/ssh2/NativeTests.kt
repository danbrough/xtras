package org.danbrough.ssh2

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.IntVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.get
import kotlinx.cinterop.getBytes
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.cinterop.write
import org.danbrough.nativetests.message
import org.danbrough.nativetests.test1
import org.danbrough.nativetests.test2
import kotlin.test.Test

@OptIn(ExperimentalForeignApi::class)
class NativeTests {
  @Test
  fun test() {
    log.info { "test()" }
    message()


    memScoped {
      val n = alloc<IntVar>(){
        value = 12345
      }



      test1(n.ptr)
      log.warn { "n is ${n.value}" }








    }
  }
}