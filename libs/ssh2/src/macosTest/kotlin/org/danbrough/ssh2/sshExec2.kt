package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION_BLOCK_INBOUND
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION_BLOCK_OUTBOUND
import org.danbrough.ssh2.cinterops.libssh2_session_block_directions
import org.danbrough.ssh2.cinterops.libssh2_socket_t
import org.danbrough.xtras.support.getEnv
import platform.posix.fd_set
import platform.posix.posix_FD_SET
import platform.posix.posix_FD_ZERO
import platform.posix.select
import platform.posix.time_tVar
import platform.posix.timer_tVar
import platform.posix.timeval
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalCoroutinesApi::class)
fun mainSshExec2(args: Array<String>) {
  log.info { "mainSshExec2()" }
  initSessionConfig(args)

  log.debug { "config: $sessionConfig" }
  val sallysPrivateKeyPath = getEnv("SSH_PRIVATE_KEY") ?: error("\$SSH_PRIVATE_KEY not set")
  //val sallysPrivateKey = SystemFileSystem.source(Path(sallysPrivateKeyPath)).buffered().readString()
  val sallysKeyPassphrase = "password"
  memScoped {
    runBlocking {
      ssh {

        var time = Clock.System.now().toEpochMilliseconds()
        val getTime: () -> Long = {
          Clock.System.now().toEpochMilliseconds().let { now ->
            (now - time).also {
              time = now
            }
          }
        }

        val session = SSHSession(this)
        log.trace { "created session $session" }
        session.connect("127.0.0.1", 2222)
        session.authenticate("sally", privateKeyPath = sallysPrivateKeyPath, password = "password")
        session.use {
          session.channel().use { channel ->
            log.info { "created channel: $channel" }
            channel.shell()
            log.debug { "opened shell pthread:${platform.posix.pthread_self()}" }
            val readJob = launch {
              log.warn { "launched read job: pthread:${platform.posix.pthread_self()}" }
              channel.readLoop()
            }
            log.trace { "launched read job .. writing date.." }
            channel.date()
            log.trace { "${getTime()}: date returned" }
            log.trace { "${getTime()}: calling date " }
            channel.date()
            log.trace { "${getTime()}: date returned" }
            delay(1.seconds)

            channel.date()
            log.trace { "${getTime()}: date returned" }
            delay(1.seconds)
            channel.date()
            log.trace { "${getTime()}: date returned" }
            delay(1.seconds)
            log.trace { "${getTime()}: cancelling job" }
            readJob.cancelAndJoin()
            log.error { "finished" }

          }

        }
        /* val channel = session.channel()
         log.error { "created channel $channel" }
         async {
           channel.execTest()

         }.getCompletionExceptionOrNull()?.also {
           log.warn { "async finished: ${it.message}" }
           channel.close()
           session.close()
         }*/
      }
    }
    log.debug { "finished blocking" }
  }

}

