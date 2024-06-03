package org.danbrough.ssh2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.danbrough.xtras.support.getEnv
import platform.posix.read
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class NativeTests {
  @Test
  fun test1() {
    log.info { "test1()" }
    val sallysPrivateKeyPath = getEnv("SSH_PRIVATE_KEY") ?: error("\$SSH_PRIVATE_KEY not set")
    //val sallysPrivateKey = SystemFileSystem.source(Path(sallysPrivateKeyPath)).buffered().readString()
    val sallysKeyPassphrase = "password"
    log.trace { "sallysPrivateKeyPath: $sallysPrivateKeyPath" }

    runBlocking {
      ssh {
        log.trace { "running in $this " }

        session {
          log.trace { "running in $this" }
          connect("127.0.0.1", 2222)
          authenticate("sally", privateKeyPath = sallysPrivateKeyPath, password = "password")

          channel {
            val prompt = "test1"
            log.trace { "running in $this" }
            exec("for i in {0..10}; do echo $prompt: i is \$i; sleep 1; done")
						readLoop()
          }

          log.info { "done" }
        }
      }
    }
  }


}