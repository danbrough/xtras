package org.danbrough.ssh2

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.libssh2_channel_close
import org.danbrough.ssh2.cinterops.libssh2_channel_exec2
import org.danbrough.ssh2.cinterops.libssh2_channel_free
import org.danbrough.ssh2.cinterops.libssh2_channel_read_ex
import platform.posix.read

class Channel(private val session: Session, private val channel: CPointer<LIBSSH2_CHANNEL>) :
  AutoCloseable {

  fun exec(commandline: String) {
    log.info { "exec:() $commandline" }
    var rc: Int
    while (libssh2_channel_exec2(channel, commandline).also { rc = it } == LIBSSH2_ERROR_EAGAIN)
      session.waitSocket()

    log.trace { "libssh2_channel_exec2() returned $rc" }
    if (rc != 0) error("libssh2_channel_exec2($commandline) returned $rc")
  }

  /*
  for(;;) {
        ssize_t nread;
        /* loop until we block */
        do {
            char buffer[0x4000];
            nread = libssh2_channel_read(channel, buffer, sizeof(buffer));
            if(nread > 0) {
                ssize_t i;
                bytecount += nread;
                fprintf(stderr, "We read:\n");
                for(i = 0; i < nread; ++i)
                    fputc(buffer[i], stderr);
                fprintf(stderr, "\n");
            }
            else {
                if(nread != LIBSSH2_ERROR_EAGAIN)
                    /* no need to output this for the EAGAIN case */
                    fprintf(stderr, "libssh2_channel_read returned %ld\n",
                            (long)nread);
            }
        }
        while(nread > 0);

        /* this is due to blocking that would occur otherwise so we loop on
           this condition */
        if(nread == LIBSSH2_ERROR_EAGAIN) {
            waitsocket(sock, session);
        }
        else
            break;
    }
   */

  fun readLoop() {
    var readCount = 0L
    memScoped {
      while (true) {
        do {
          val buffer = ByteArray(0x4000) //allocArray<ByteVar>(0x4000)
          buffer.usePinned {
            readCount = libssh2_channel_read_ex(channel, 0, it.addressOf(0), buffer.size.convert())
            if (readCount > 0) {
              log.info { "readCount: $readCount <${buffer.decodeToString(0,readCount.convert())}>" }
            } else {
              if (readCount != LIBSSH2_ERROR_EAGAIN.toLong() && readCount != 0L)
                error("libssh2_channel_read_ex returned $readCount")
            }
          }
        } while(readCount > 0L)

        if (readCount == LIBSSH2_ERROR_EAGAIN.toLong())
          session.waitSocket()
        else
          break
      }
    }
  }

  override fun close() {
    log.trace { "Channel::close()" }

    var rc:Int
    while(libssh2_channel_close(channel).also { rc = it } == LIBSSH2_ERROR_EAGAIN)
      session.waitSocket()
    log.trace { "closed channel: rc: $rc" }
    libssh2_channel_free(channel)


    /*
        while((rc = libssh2_channel_close(channel)) == LIBSSH2_ERROR_EAGAIN)
        waitsocket(sock, session);

    if(rc == 0) {
        exitcode = libssh2_channel_get_exit_status(channel);
        libssh2_channel_get_exit_signal(channel, &exitsignal,
                                        NULL, NULL, NULL, NULL, NULL);
    }

    if(exitsignal)
        fprintf(stderr, "\nGot signal: %s\n", exitsignal);
    else
        fprintf(stderr, "\nEXIT: %d bytecount: %ld\n",
                exitcode, (long)bytecount);

    libssh2_channel_free(channel);
     */

  }
}