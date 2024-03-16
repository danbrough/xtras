package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_PACKET_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_WINDOW_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOSTS
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_MISMATCH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_FILE_OPENSSH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_RAW
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_PLAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION_BLOCK_INBOUND
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION_BLOCK_OUTBOUND
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.cinterops.libssh2_channel_open_ex
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost
import org.danbrough.ssh2.cinterops.libssh2_knownhost_checkp
import org.danbrough.ssh2.cinterops.libssh2_knownhost_free
import org.danbrough.ssh2.cinterops.libssh2_knownhost_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost_readfile
import org.danbrough.ssh2.cinterops.libssh2_session_block_directions
import org.danbrough.ssh2.cinterops.libssh2_session_disconnect_ex
import org.danbrough.ssh2.cinterops.libssh2_session_free
import org.danbrough.ssh2.cinterops.libssh2_session_handshake
import org.danbrough.ssh2.cinterops.libssh2_session_hostkey
import org.danbrough.ssh2.cinterops.libssh2_session_init_ex
import org.danbrough.ssh2.cinterops.libssh2_session_last_error
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_socket_t

import org.danbrough.ssh2.cinterops.libssh2_userauth_publickey_fromfile_ex
import org.danbrough.xtras.support.supportLog
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.fd_set
import platform.posix.htons
import platform.posix.posix_FD_SET
import platform.posix.posix_FD_ZERO
import platform.posix.select
import platform.posix.shutdown
import platform.posix.size_t
import platform.posix.size_tVar
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror
import platform.posix.timeval
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

val log = run {
  supportLog.trace { }
  KotlinLogging.logger("TESTS")
}

object TestConfig {
  fun property(name: String, default: String): String = platform.posix.getenv(name)?.toKString() ?: default

  val user = "dan"
  val hostname = "192.168.1.4"
  val port = 22.toUShort()
  val pubKey = "/home/dan/.ssh/test.pub"
  val privKey = "/home/dan/.ssh/test"
  val password = "password"
}


class SSHTests {

  @Test
  fun test() {
    log.trace { "test() trace" }
    log.info { "test()" }
    log.error { "test() error" }
  }

  @Test
  fun testExec() {
    log.info { "testExec()" }


    runBlocking {
      var sock: libssh2_socket_t = 0
      var session: CPointer<LIBSSH2_SESSION>? = null
      var nh: CPointer<LIBSSH2_KNOWNHOSTS>? = null
      var rc = 0
      var type = 0
      var len: size_t = 0.toULong()

      /*
          uint32_t hostaddr;
    libssh2_socket_t sock;
    struct sockaddr_in sin;
    const char *fingerprint;
    int rc;
    LIBSSH2_SESSION *session = NULL;
    LIBSSH2_CHANNEL *channel;
    int exitcode;
    char *exitsignal = (char *)"none";
    ssize_t bytecount = 0;
    size_t len;
    LIBSSH2_KNOWNHOSTS *nh;
    int type;
       */



      launch {
        memScoped {

          rc = libssh2_init(0)
          log.debug { "got rc: $rc" }
          if (rc != 0) {
            error("libssh2 initialization failed ($rc)")
          }

          val hostaddr = inet_addr(TestConfig.hostname)

          sock = socket(AF_INET, SOCK_STREAM, 0)
          if (sock == LIBSSH2_INVALID_SOCKET) {
            error("failed to create socket!")
          }

          val sin = cValue<sockaddr_in> {
            sin_family = AF_INET.toUShort()
            sin_port = htons(TestConfig.port)
            sin_addr.s_addr = hostaddr
          }

          connect(sock, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().toUInt()).also {
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
          nh = libssh2_knownhost_init(session) ?: error("libssh2_knownhost_init failed")

          log.trace { "libssh2_knownhost_readFile" }
          libssh2_knownhost_readfile(
            nh, "/home/dan/.ssh/known_hosts",
            LIBSSH2_KNOWNHOST_FILE_OPENSSH
          )


          val keyType = alloc<IntVar>()
          val keyLength = alloc<size_tVar>()
          val fingerprint = libssh2_session_hostkey(session, keyLength.ptr, keyType.ptr)
            ?: error("Failed to get session fingerprint")

          log.info { "keyLength: ${keyLength.value} keyType: ${keyType.value}" }
          val fingerprintString = fingerprint.readBytes(keyLength.value.toInt())
          log.info { "FINGERPRINT: ${Base64.encode(fingerprintString)}" }

          val knownHost = cValue<libssh2_knownhost>()
          val check = libssh2_knownhost_checkp(
            nh,
            TestConfig.hostname,
            TestConfig.port.toInt(),
            fingerprintString.toKString(),
            keyLength.value,
            LIBSSH2_KNOWNHOST_TYPE_PLAIN or LIBSSH2_KNOWNHOST_KEYENC_RAW,// or LIBSSH2_KNOWNHOST_TYPE_SHA1,
            knownHost.ptr.reinterpret()
          )

          val ok = check <= LIBSSH2_KNOWNHOST_CHECK_MISMATCH
          log.info { "Host check: $check" }
          /*
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
           */

          libssh2_knownhost_free(nh)

          do {
            rc = libssh2_userauth_publickey_fromfile_ex(
              session,
              TestConfig.user,
              TestConfig.user.length.toUInt(),
              TestConfig.pubKey,
              TestConfig.privKey,
              TestConfig.password
            )
          } while (rc == LIBSSH2_ERROR_EAGAIN)

          if (rc != 0) error("libssh2_userauth_publickey_fromfile_ex returned $rc")

          /*


        while((rc = libssh2_userauth_publickey_fromfile(session, username,
                                                        pubkey, privkey,
                                                        password)) ==
              LIBSSH2_ERROR_EAGAIN);
        if(rc) {
            fprintf(stderr, "Authentication by public key failed.\n");
            goto shutdown;
        }
           */

          val ss = "session".cstr.size

          log.trace { "(\"session\".cstr.size - 1) == ${"session".cstr.size - 1}" }

          var channel: CPointer<LIBSSH2_CHANNEL>? = null

          while (true) {
            channel = libssh2_channel_open_ex(
              session,
              "session",
              ("session".cstr.size - 1).toUInt(),
              LIBSSH2_CHANNEL_WINDOW_DEFAULT.toUInt(),
              LIBSSH2_CHANNEL_PACKET_DEFAULT.toUInt(),
              null,
              0u
            )
            if (channel != null ||
              libssh2_session_last_error(session, null, null, 0) != LIBSSH2_ERROR_EAGAIN
            ) break
            waitSocket(sock, session!!)
          }

          log.debug { "channel: $channel" }

          /*

          #define libssh2_channel_open_session(session) \
    libssh2_channel_open_ex((session), "session", sizeof("session") - 1, \
                            LIBSSH2_CHANNEL_WINDOW_DEFAULT, \
                            LIBSSH2_CHANNEL_PACKET_DEFAULT, NULL, 0)

             // Exec non-blocking on the remote host
    do {
        channel = libssh2_channel_open_session(session);
        if(channel ||
           libssh2_session_last_error(session, NULL, NULL, 0) !=
           LIBSSH2_ERROR_EAGAIN)
            break;
        waitsocket(sock, session);
    } while(1);
    if(!channel) {
        fprintf(stderr, "Error\n");
        exit(1);
    }
    while((rc = libssh2_channel_exec(channel, commandline)) ==
          LIBSSH2_ERROR_EAGAIN) {
        waitsocket(sock, session);
    }
    if(rc) {
        fprintf(stderr, "exec error\n");
        exit(1);
    }
           */
          delay(1.seconds)
          log.debug { "exiting.." }
          libssh2_exit()
        }

      }.invokeOnCompletion { err ->
        log.info { "finishing.." }

        session?.also {
          log.info { "libssh2_session_disconnect_ex" }
          libssh2_session_disconnect_ex(
            it.reinterpret(),
            SSH_DISCONNECT_BY_APPLICATION,
            "Normal Shutdown",
            ""
          ).also {
            log.trace { "libssh2_session_disconnect_ex returned $it" }
          }
          log.info { "libssh2_session_free" }
          libssh2_session_free(it).also {
            log.trace { "libssh2_session_free returned: $it" }
          }
        }

        if (sock != LIBSSH2_INVALID_SOCKET) {
          log.trace { "shutdown socket.." }
          shutdown(sock, 2)
          close(sock)
        }

        if (err != null)
          log.error(err) { "got an error: ${err.message}" }

        log.info { "exiting." }
      }
    }

  }

  private fun waitSocket(socketFd: libssh2_socket_t, session: CPointer<LIBSSH2_SESSION>): Int {

    memScoped {
      val timeout = cValue<timeval> {
        tv_sec = 10
        tv_usec = 0
      }
      val fd = cValue<fd_set>()
      var writeFD: CPointer<fd_set>? = null
      var readFD: CPointer<fd_set>? = null
      posix_FD_ZERO(fd)

      posix_FD_SET(socketFd, fd)

      log.trace { "libssh2_session_block_directions" }
      val dir = libssh2_session_block_directions(session)
      log.trace { "libssh2_session_block_directions done dir:$dir" }
      if ((dir and LIBSSH2_SESSION_BLOCK_INBOUND) != 0)
        readFD = fd.ptr

      if ((dir and LIBSSH2_SESSION_BLOCK_OUTBOUND) != 0)
        writeFD = fd.ptr

      return select(socketFd + 1, readFD, writeFD, null, timeout.ptr).also {
        log.trace { "select returned $it" }

      }

    }
//    struct timeval timeout;
//    int rc;
//    fd_set fd;
//    fd_set *writefd = NULL;
//    fd_set *readfd = NULL;
//    int dir;
//
//    timeout.tv_sec = 10;
//    timeout.tv_usec = 0;
//
//    FD_ZERO(&fd);
//
//    FD_SET(socket_fd, &fd);
//
//    /* now make sure we wait in the correct direction */
//    dir = libssh2_session_block_directions(session);
//
//    if(dir & LIBSSH2_SESSION_BLOCK_INBOUND)
//    readfd = &fd;
//
//    if(dir & LIBSSH2_SESSION_BLOCK_OUTBOUND)
//    writefd = &fd;
//
//    rc = select((int)(socket_fd + 1), readfd, writefd, NULL, &timeout);
//
//    return rc;
  }
}