package org.danbrough.ssh2


import kotlinx.cinterop.*
import org.danbrough.nativetests.*
import kotlin.test.Test

class NativeTests {
    @Test
    fun test() {
        log.info { "test()" }
        message()


        memScoped {
            val n = alloc<IntVar> {
                value = 12345
            }

            test1(n.ptr)
            log.warn { "n is ${n.value}" }
            test3()

            log.warn { "MESSAGE: ${MESSAGE!!.toKString()}" }


            val t: Thang = alloc<Thang>()
            t.n = 12
            t.d = 123.456
            log.warn { "thang: ${t.n}:${t.d}" }


            testThang(t.ptr)
            log.warn { "thang: ${t.n}:${t.d}" }

            testThang(t.ptr)
            log.warn { "thang: ${t.n}:${t.d}" }

            /*
                        val counter = alloc<thang_counter_tVar>() {
                            value = 123u
                        }

                        counter.value++
                        log.info { "counter value: ${counter.value}" }
            */


        }
    }
}