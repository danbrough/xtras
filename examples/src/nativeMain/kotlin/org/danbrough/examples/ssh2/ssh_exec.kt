package org.danbrough.examples.ssh2

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import org.danbrough.examples.TestConfig
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_PACKET_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_WINDOW_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_MISMATCH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_FILE_OPENSSH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_RAW
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_PLAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.cinterops.libssh2_channel_close
import org.danbrough.ssh2.cinterops.libssh2_channel_exec2
import org.danbrough.ssh2.cinterops.libssh2_channel_free
import org.danbrough.ssh2.cinterops.libssh2_channel_get_exit_signal
import org.danbrough.ssh2.cinterops.libssh2_channel_get_exit_status
import org.danbrough.ssh2.cinterops.libssh2_channel_open_ex
import org.danbrough.ssh2.cinterops.libssh2_channel_read_ex
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost
import org.danbrough.ssh2.cinterops.libssh2_knownhost_checkp
import org.danbrough.ssh2.cinterops.libssh2_knownhost_free
import org.danbrough.ssh2.cinterops.libssh2_knownhost_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost_readfile
import org.danbrough.ssh2.cinterops.libssh2_knownhost_writefile
import org.danbrough.ssh2.cinterops.libssh2_session_disconnect_ex
import org.danbrough.ssh2.cinterops.libssh2_session_handshake
import org.danbrough.ssh2.cinterops.libssh2_session_hostkey
import org.danbrough.ssh2.cinterops.libssh2_session_init_ex
import org.danbrough.ssh2.cinterops.libssh2_session_last_error
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_socket_t
import org.danbrough.ssh2.cinterops.libssh2_userauth_publickey_fromfile_ex
import org.danbrough.ssh2.cinterops.waitsocket
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.fprintf
import platform.posix.fputc
import platform.posix.htons
import platform.posix.shutdown
import platform.posix.size_tVar
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.stderr
import platform.posix.strerror
import kotlin.io.encoding.Base64


fun sshExec() {
  log.info { "ssh2Exec()" }


  var rc = 0
  var sock: libssh2_socket_t = 0
  var session: CPointer<LIBSSH2_SESSION>? = null
  libssh2_init(0).also {
    if (it != 0) error("libssh2 initialization failed ($it)")
  }

  val hostaddr = inet_addr(TestConfig.HOSTNAME)

  sock = socket(AF_INET, SOCK_STREAM, 0)
  if (sock == LIBSSH2_INVALID_SOCKET) {
    error("failed to create socket!")
  }

  memScoped {
    runCatching {

      val sin = cValue<sockaddr_in> {
        sin_family = AF_INET.toUShort()
        sin_port = htons(TestConfig.PORT)
        sin_addr.s_addr = hostaddr
      }


      connect(sock, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()).also {
        log.trace { "connected returned $it" }
        if (it != 0)
          error("Failed to connect: ${strerror(it)?.toKString()}")
      }

      session = libssh2_session_init_ex(null, null, null, null)
        ?: error("Failed to created ssh session")

      libssh2_session_set_blocking(session, 0)

      do {
        rc = libssh2_session_handshake(session, sock)
      } while (rc == LIBSSH2_ERROR_EAGAIN)

      if (rc != 0)
        error("Failure establishing SSH session: $rc: ${strerror(rc)?.toKString()}")

      log.trace { "libssh2_knownhost_init(session)" }
      val nh = libssh2_knownhost_init(session) ?: error("libssh2_knownhost_init failed")


      TestConfig.knownHostsInput?.also {
        /* read all hosts from here */
        log.debug { "reading known hosts from $it" }
        libssh2_knownhost_readfile(
          nh, it,
          LIBSSH2_KNOWNHOST_FILE_OPENSSH
        ).also {
          log.debug { "read known hosts returned $it" }
        }
      }

      /* store all known hosts to here */
      TestConfig.knownHostsOutput?.also {
        log.debug { "writing known hosts to $it" }
        libssh2_knownhost_writefile(
          nh, it,
          LIBSSH2_KNOWNHOST_FILE_OPENSSH
        ).also {
          log.debug { "write known hosts returned $it" }
        }
      }

      /*
          fingerprint = libssh2_session_hostkey(session, &len, &type);
    if(fingerprint) {
        struct libssh2_knownhost *host;
        int check = libssh2_knownhost_checkp(nh, hostname, 22,
                                             fingerprint, len,
                                             LIBSSH2_KNOWNHOST_TYPE_PLAIN|
                                             LIBSSH2_KNOWNHOST_KEYENC_RAW,
                                             &host);

        fprintf(stderr, "Host check: %d, key: %s\n", check,
                (check <= LIBSSH2_KNOWNHOST_CHECK_MISMATCH) ?
                host->key : "<none>");

        /*****
         * At this point, we could verify that 'check' tells us the key is
         * fine or bail out.
         *****/
    }
    else {
        /* eeek, do cleanup here */
        return 3;
    }
    libssh2_knownhost_free(nh);
       */

      val keyType = alloc<IntVar>()
      val keyLength = alloc<size_tVar>()
      val fingerprint = libssh2_session_hostkey(session, keyLength.ptr, keyType.ptr)
        ?: error("Failed to get session fingerprint")

      log.info { "keyLength: ${keyLength.value} keyType: ${keyType.value}" }
      val fingerprintString = fingerprint.readBytes(keyLength.value.toInt())
      log.info { "FINGERPRINT: ${Base64.encode(fingerprintString)}" }


      //val knownHost = cValue<libssh2_knownhost>()
      val host: CPointerVar<libssh2_knownhost> = alloc()
      val check = libssh2_knownhost_checkp(
        nh,
        TestConfig.HOSTNAME,
        TestConfig.PORT.toInt(),
        fingerprintString.toKString(),
        keyLength.value,
        LIBSSH2_KNOWNHOST_TYPE_PLAIN or LIBSSH2_KNOWNHOST_KEYENC_RAW,// or LIBSSH2_KNOWNHOST_TYPE_SHA1,
        host.ptr.reinterpret()
      )

      log.info { "key ${host.pointed?.key?.toKString()} name:${host.pointed?.name?.toKString()}" }
      log.warn { "Host check: $check key: ${if (check <= LIBSSH2_KNOWNHOST_CHECK_MISMATCH) host.pointed?.key?.toKString() else "<none>"}" }


      /*
             fprintf(stderr, "Host check: %d, key: %s\n", check,
                (check <= LIBSSH2_KNOWNHOST_CHECK_MISMATCH) ?
                host->key : "<none>");
       */


      libssh2_knownhost_free(nh)

      do {
        rc = libssh2_userauth_publickey_fromfile_ex(
          session,
          TestConfig.USER,
          TestConfig.USER.length.convert(),
          TestConfig.PUB_KEY,
          TestConfig.PRIVATE_KEY,
          TestConfig.PASSWORD
        )
      } while (rc == LIBSSH2_ERROR_EAGAIN)

      if (rc != 0) error("libssh2_userauth_publickey_fromfile_ex returned $rc")


      var channel: CPointer<LIBSSH2_CHANNEL>? = null

      while (true) {
        channel = libssh2_channel_open_ex(
          session,
          "session",
          "session".length.toUInt(),
          LIBSSH2_CHANNEL_WINDOW_DEFAULT.toUInt(),
          LIBSSH2_CHANNEL_PACKET_DEFAULT.toUInt(),
          null,
          0u
        )
        if (channel != null ||
          libssh2_session_last_error(session, null, null, 0) != LIBSSH2_ERROR_EAGAIN
        ) break

        waitsocket(sock, session)
        //waitSocket(sock, session!!)
      }

      channel?.also {
        log.info { "created channel $it" }
      } ?: error("open channel failed")


      log.trace { "libssh2_channel_exec2 commandLine: ${TestConfig.COMMAND_LINE}" }
      while (libssh2_channel_exec2(channel, TestConfig.COMMAND_LINE).also {
          rc = it
        } == LIBSSH2_ERROR_EAGAIN) {
        waitsocket(sock, session)
      }

      if (rc != 0) error("libssh2_channel_exec2 failed: $rc")

      var byteCount: Long = 0

      while (true) {
        var readCount: Long = 0
        do {
          val buffer = ByteArray(0x4000)
          buffer.usePinned {
            readCount = libssh2_channel_read_ex(channel, 0, it.addressOf(0), buffer.size.convert())
            log.trace { "read: $readCount" }
            if (readCount > 0) {
              byteCount += readCount
              fprintf(stderr, "We read:\n")
              for (i in 0 until readCount.convert()) {
                fputc(buffer[i].convert(), stderr)
              }
              fprintf(stderr, "\n")
            } else {
              if (readCount != LIBSSH2_ERROR_EAGAIN.toLong())
                fprintf(stderr, "libssh2_channel_read returned $readCount\n")
            }
            Unit
          }
        } while (readCount > 0)

        /* this is due to blocking that would occur otherwise so we loop on
       this condition */
        if (readCount == LIBSSH2_ERROR_EAGAIN.toLong()) {
          waitsocket(sock, session);
        } else {
          log.trace { "breaking from loop as rc == $rc" }
          break;
        }
      }

//      exitcode = 127;
//      while((rc = libssh2_channel_close(channel)) == LIBSSH2_ERROR_EAGAIN)
//        waitsocket(sock, session);
//
//      if(rc == 0) {
//        exitcode = libssh2_channel_get_exit_status(channel);
//        libssh2_channel_get_exit_signal(channel, &exitsignal,
//        NULL, NULL, NULL, NULL, NULL);
//      }
//
//      if(exitsignal)
//        fprintf(stderr, "\nGot signal: %s\n", exitsignal);
//      else
//        fprintf(stderr, "\nEXIT: %d bytecount: %ld\n",
//          exitcode, (long)bytecount);
//
//      libssh2_channel_free(channel);
//      channel = NULL;

      while (libssh2_channel_close(channel).also {
          rc = it
        } == LIBSSH2_ERROR_EAGAIN) {
        waitsocket(sock, session)
      }
      log.trace { "libssh2_channel_close(channel) == $rc" }
      var exitCode = 127
      val exitSignal = this@memScoped.allocArray<ByteVar>(64)
      exitSignal.usePinned {
        if (rc == 0) {
          exitCode = libssh2_channel_get_exit_status(channel)
          libssh2_channel_get_exit_signal(
            channel,
            exitSignal.reinterpret(),
            null,
            null,
            null,
            null,
            null
          )
          log.info { "exitCode: $exitCode byteCount: $byteCount exitSignal: ${exitSignal.toKString()}" }
        }
      }
      libssh2_channel_free(channel)
      channel = null

    }
  }.exceptionOrNull().also {
    if (it != null) log.error(it) { it.message }
    if (session != null) {
      log.trace { "calling libssh2_session_disconnect_ex" }
      libssh2_session_disconnect_ex(session, SSH_DISCONNECT_BY_APPLICATION, "Normal Shutdown", "")
    }

    if (sock != LIBSSH2_INVALID_SOCKET) {
      log.trace { "shutdown socket.." }
      shutdown(sock, 2)
      //libssh2_socket_close2(sock)
      //libssh2_socket_close2(sock)
      close(sock)
    }

    libssh2_exit()

//    if(sock != LIBSSH2_INVALID_SOCKET) {
//      shutdown(sock, 2);
//      LIBSSH2_SOCKET_CLOSE(sock);
//    }
//
//    fprintf(stderr, "all done\n");
//
//    libssh2_exit();
//
//    #ifdef _WIN32
//      WSACleanup();
//    #endif

  }

}