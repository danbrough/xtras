package org.danbrough.ssh2

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class JvmTests {
  /*  @Test
    fun testSshExec() {
      initSessionConfig(emptyArray())
      sshExec()
    }*/

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testCoroutines() {
    runBlocking {
      val job = launch {
        delay(2.seconds)
        throw Exception("Test")
      }
      log.debug { "getting completion.." }

    }

//      .getCompletionExceptionOrNull().also {
//      log.warn { "finished: ${it?.message}" }
//    }
  }
}