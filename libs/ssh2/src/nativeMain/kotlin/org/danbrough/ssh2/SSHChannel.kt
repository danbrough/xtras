package org.danbrough.ssh2

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_PACKET_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_WINDOW_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.libssh2_channel_close
import org.danbrough.ssh2.cinterops.libssh2_channel_get_exit_status
import org.danbrough.ssh2.cinterops.libssh2_channel_open_ex
import org.danbrough.ssh2.cinterops.libssh2_channel_process_startup
import org.danbrough.ssh2.cinterops.libssh2_channel_read_ex
import org.danbrough.ssh2.cinterops.libssh2_channel_write_ex
import org.danbrough.ssh2.cinterops.libssh2_session_last_errno
import org.danbrough.ssh2.cinterops.waitsocket
import platform.posix.size_t
import platform.posix.ssize_t

class SSHChannel(private val session: SSHSession, private val channel: CPointer<LIBSSH2_CHANNEL>) :
  Scope {


  fun exec(commandLine: String) {
    log.debug { "exec() $commandLine" }
    var rc: Int

    val processType = "exec" // "shell", "exec" or "subsystem"

    while (libssh2_channel_process_startup(
        channel,
        processType,
        processType.length.convert(),
        commandLine,
        commandLine.length.convert()
      ).also { rc = it } == LIBSSH2_ERROR_EAGAIN
    ) session.waitSocket()

    log.trace { "libssh2_channel_process_startup() returned $rc" }
    if (rc != 0) error("libssh2_channel_process_startup($commandLine) returned $rc")
  }


  fun shell() {
    log.debug { "shell()" }
    var rc: Int

    val processType = "shell" // "shell", "exec" or "subsystem"

    while (libssh2_channel_process_startup(
        channel,
        processType,
        processType.length.convert(),
        null,
        0.convert()
      ).also { rc = it } == LIBSSH2_ERROR_EAGAIN
    ) session.waitSocket()

    log.trace { "libssh2_channel_process_startup() returned $rc" }
    if (rc != 0) error("libssh2_channel_process_startup(shell) returned $rc")
  }

  suspend fun date() {
    log.warn { "date()" }
//    exec("sh")
    //  log.info { "started sh" }
    var rc: ssize_t
    do {
      val cmd = "date\n"
      rc = libssh2_channel_write_ex(channel, 0, cmd, cmd.length.toULong())
    } while (rc == LIBSSH2_ERROR_EAGAIN.toLong())


    if (rc < 0) log.error { "rc = $rc" }
    log.info { "ran date" }

  }


  suspend fun readLoop() =
    session.ssh.memScoped {

      var readCount = 0L

      while (true) {
        do {
          val buffer = ByteArray(0x4000) //allocArray<ByteVar>(0x4000)

          buffer.usePinned {
            readCount =
              libssh2_channel_read_ex(channel, 0, it.addressOf(0), buffer.size.convert())
            if (readCount > 0) {
              log.info {
                "readCount: $readCount <${
                  buffer.decodeToString(
                    0,
                    readCount.convert()
                  )
                }>"
              }
            } else {
              if (readCount != LIBSSH2_ERROR_EAGAIN.toLong() && readCount != 0L)
                error("libssh2_channel_read_ex returned $readCount")
            }
          }
        } while (readCount > 0L)

        if (readCount == LIBSSH2_ERROR_EAGAIN.toLong()) {
          delay(100)
          //session.waitSocket()
        } else break
      }
    }


  override fun close() {
    log.trace { "Channel::close()" }

    var rc: Int
    while (libssh2_channel_close(channel).also { rc = it } == LIBSSH2_ERROR_EAGAIN)
      session.waitSocket()
    log.trace { "libssh2_channel_close() -> $rc" }

    if (rc == 0) {
      libssh2_channel_get_exit_status(channel).also {
        log.trace { "libssh2_channel_get_exit_status(channel) -> $it" }
      }
    }

  }

}