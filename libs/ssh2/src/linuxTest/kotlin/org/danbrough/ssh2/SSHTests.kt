package org.danbrough.ssh2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOSTS
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_MISMATCH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_FILE_OPENSSH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_BASE64
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_RAW
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_PLAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_SHA1
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost
import org.danbrough.ssh2.cinterops.libssh2_knownhost_checkp
import org.danbrough.ssh2.cinterops.libssh2_knownhost_free
import org.danbrough.ssh2.cinterops.libssh2_knownhost_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost_readfile
import org.danbrough.ssh2.cinterops.libssh2_session_disconnect_ex
import org.danbrough.ssh2.cinterops.libssh2_session_free
import org.danbrough.ssh2.cinterops.libssh2_session_handshake
import org.danbrough.ssh2.cinterops.libssh2_session_hostkey
import org.danbrough.ssh2.cinterops.libssh2_session_init_ex
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_userauth_publickey_fromfile_ex
import org.danbrough.xtras.support.supportLog
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.htons
import platform.posix.shutdown
import platform.posix.size_t
import platform.posix.size_tVar
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

val log = run {
  supportLog.trace { }
  KotlinLogging.logger("TESTS")
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

    val config = object {
      val user = "dan"
      val hostname = "192.168.1.4"
      val port = 22.toUShort()
      val pubKey = "/home/dan/.ssh/id_ed25519.pub"
      val privKey = "/home/dan/.ssh/id_ed25519"
    }

    runBlocking {
      var sock = 0
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

          rc = libssh2_init(0);
          log.debug { "got rc: $rc" }
          if (rc != 0) {
            error("libssh2 initialization failed ($rc)")
          }

          val hostaddr = inet_addr(config.hostname)

          sock = socket(AF_INET, SOCK_STREAM, 0)
          if (sock == LIBSSH2_INVALID_SOCKET) {
            error("failed to create socket!")
          }

          val sin = cValue<sockaddr_in>() {
            sin_family = AF_INET.toUShort()
            sin_port = htons(config.port)
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
            config.hostname,
            config.port.toInt(),
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
           rc =  libssh2_userauth_publickey_fromfile_ex(
              session,
              config.user,
              config.user.length.toUInt(),
              config.pubKey,
              config.privKey,
              "poiqwe"
            )
          } while (rc == LIBSSH2_ERROR_EAGAIN)

          log.info { "libssh2_userauth_publickey_fromfile_ex returned $rc" }

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
}