package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.libssh2_channel_close
import org.danbrough.ssh2.cinterops.libssh2_channel_free
import org.danbrough.ssh2.cinterops.libssh2_channel_get_exit_status
import org.danbrough.ssh2.cinterops.libssh2_channel_process_startup
import org.danbrough.ssh2.cinterops.libssh2_channel_read_ex

class Channel(private val session: SessionNative, private val channel: CPointer<LIBSSH2_CHANNEL>) :
  AutoCloseable {

  fun exec(commandline: String) {
    log.info { "exec:() $commandline" }
    var rc: Int

    val processType = "exec" // "shell", "exec" or "subsystem"

    while (libssh2_channel_process_startup(
        channel,
        processType,
        processType.length.convert(),
        commandline,
        commandline.length.convert()
      ).also { rc = it } == LIBSSH2_ERROR_EAGAIN
    ) session.waitSocket()

    log.trace { "libssh2_channel_process_startup() returned $rc" }
    if (rc != 0) error("libssh2_channel_process_startup($commandline) returned $rc")
  }

  fun readLoop() = memScoped {
    var readCount = 0L

    while (true) {
      do {
        val buffer = ByteArray(0x4000) //allocArray<ByteVar>(0x4000)
        buffer.usePinned {
          readCount = libssh2_channel_read_ex(channel, 0, it.addressOf(0), buffer.size.convert())
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

      if (readCount == LIBSSH2_ERROR_EAGAIN.toLong())
        session.waitSocket() else break
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

    libssh2_channel_free(channel)
  }
}