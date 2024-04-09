@file:OptIn(ExperimentalForeignApi::class)

package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_HASH_SHA1
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_HASH_SHA256
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_DSS
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_ECDSA_256
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_ECDSA_384
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_ECDSA_521
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_ED25519
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_RSA
import org.danbrough.ssh2.cinterops.LIBSSH2_HOSTKEY_TYPE_UNKNOWN
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOSTS
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_FAILURE
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_MATCH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_MISMATCH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_CHECK_NOTFOUND
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_FILE_OPENSSH
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_BASE64
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_MASK
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_KEYENC_RAW
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_PLAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_SHA1
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.libssh2_hostkey_hash
import org.danbrough.ssh2.cinterops.libssh2_knownhost
import org.danbrough.ssh2.cinterops.libssh2_knownhost_checkp
import org.danbrough.ssh2.cinterops.libssh2_knownhost_free
import org.danbrough.ssh2.cinterops.libssh2_knownhost_init
import org.danbrough.ssh2.cinterops.libssh2_knownhost_readfile
import org.danbrough.ssh2.cinterops.libssh2_session_handshake
import org.danbrough.ssh2.cinterops.libssh2_session_hostkey
import org.danbrough.ssh2.cinterops.libssh2_session_init_ex
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_socket_t
import org.danbrough.ssh2.cinterops.ssh2_exit
import org.danbrough.ssh2.cinterops.ssh2_init
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.connect
import platform.posix.htons
import platform.posix.size_tVar
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror
import kotlin.io.encoding.Base64

class SSH {
  fun initialize(initFlags: Int = 0) {
    ssh2_init(initFlags).also {
      if (it != 0) error("ssh2_init() returned $it")
      else log.trace { "ssh2_init()" }
    }
  }

  fun dispose() {
    log.trace { "ssh2_exit()" }
    ssh2_exit()
  }


  class Session internal constructor(@Suppress("MemberVisibilityCanBePrivate") val config: SessionConfig) {
    private var sock: libssh2_socket_t = 0
    private var session: CPointer<LIBSSH2_SESSION>? = null

    internal fun connect() {
      memScoped {
        log.info { "SSH.connect() ${config.user}@${config.hostName}:${config.port}" }

        sock = socket(AF_INET, SOCK_STREAM, 0);
        if (sock == LIBSSH2_INVALID_SOCKET)
          error("Failed to create socket")
        log.trace { "created socket" }

        val sockAddress = cValue<sockaddr_in>() {
          sin_family = AF_INET.convert()
          sin_port = htons(config.port.convert())
          sin_addr.s_addr = inet_addr(config.hostName)
        }

        connect(sock, sockAddress.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
          .also {
            log.trace { "connected returned $it" }
            if (it != 0)
              error("Failed to connect: ${strerror(it)?.toKString()}")
          }

        log.debug { "socket connected" }

        session =
          libssh2_session_init_ex(null, null, null, null)
            ?: error("Failed to created ssh session")

        libssh2_session_set_blocking(session, 0)

        /* Enable all debugging when libssh2 was built with debugging enabled */
        //libssh2_trace(session, 0)

        var rc = 0
        do {
          rc = libssh2_session_handshake(session, sock)
        } while (rc == LIBSSH2_ERROR_EAGAIN)
        if (rc != 0) error("libssh2_session_handshake(session, sock) failed. returned: $rc")
        log.debug { "handshake complete" }

        config.knownHostsFile?.also { loadKnownHosts(this, it) }

      }
    }

    private fun loadKnownHosts(memScope: MemScope, knownHostsFile: String) = memScope.apply {
      log.info { "loadKnownHosts(): ${config.knownHostsFile}" }
      var nh: CPointer<LIBSSH2_KNOWNHOSTS>? = null
      val keyType = alloc<IntVar>()
      val keyLength = alloc<size_tVar>()

      try {
        val fingerprint = libssh2_session_hostkey(session, keyLength.ptr, keyType.ptr)
          ?: error("Failed to get session fingerprint")

        /*
          ##Hash Types
          #define LIBSSH2_HOSTKEY_HASH_MD5                            1
          #define LIBSSH2_HOSTKEY_HASH_SHA1                           2
          #define LIBSSH2_HOSTKEY_HASH_SHA256                         3

          ##Hostkey Types
          #define LIBSSH2_HOSTKEY_TYPE_UNKNOWN            0
          #define LIBSSH2_HOSTKEY_TYPE_RSA                1
          #define LIBSSH2_HOSTKEY_TYPE_DSS                2
          #define LIBSSH2_HOSTKEY_TYPE_ECDSA_256          3
          #define LIBSSH2_HOSTKEY_TYPE_ECDSA_384          4
          #define LIBSSH2_HOSTKEY_TYPE_ECDSA_521          5
          #define LIBSSH2_HOSTKEY_TYPE_ED25519            6
         */

        log.debug {
          val keyTypeName = when (keyType.value) {
            LIBSSH2_HOSTKEY_TYPE_UNKNOWN -> "LIBSSH2_HOSTKEY_TYPE_UNKNOWN"
            LIBSSH2_HOSTKEY_TYPE_RSA -> "LIBSSH2_HOSTKEY_TYPE_RSA"
            LIBSSH2_HOSTKEY_TYPE_DSS -> "LIBSSH2_HOSTKEY_TYPE_DSS"
            LIBSSH2_HOSTKEY_TYPE_ECDSA_256 -> "LIBSSH2_HOSTKEY_TYPE_ECDSA_256"
            LIBSSH2_HOSTKEY_TYPE_ECDSA_384 -> "LIBSSH2_HOSTKEY_TYPE_ECDSA_384"
            LIBSSH2_HOSTKEY_TYPE_ECDSA_521 -> "LIBSSH2_HOSTKEY_TYPE_ECDSA_521"
            LIBSSH2_HOSTKEY_TYPE_ED25519 -> "LIBSSH2_HOSTKEY_TYPE_ED25519"
            else -> "Invalid LIBSSH2_HOSTKEY_TYPE: ${keyType.value}"
          }
          "keyLength: ${keyLength.value} keyType: ${keyType.value} = $keyTypeName"
        }
        val fingerprintString = fingerprint.readBytes(keyLength.value.toInt())
        log.debug { "fingerprintBase64: ${Base64.encode(fingerprintString)} fingerPrintString:${fingerprintString.toKString()} fingerPrint:${fingerprint.toKString()}" }

        nh = libssh2_knownhost_init(session) ?: error("libssh2_knownhost_init(session) failed")


        libssh2_knownhost_readfile(nh, knownHostsFile, LIBSSH2_KNOWNHOST_FILE_OPENSSH).also {
          if (it < 0) error("libssh2_knownhost_readfile($knownHostsFile) returned $it")
          else log.trace { "libssh2_knownhost_readfile($knownHostsFile) parsed $it entries" }
        }


        /*
        typemask is a bitmask that specifies format and info about the data passed to this function. Specifically, it details what format the host name is, what format the key is and what key type it is.
        The host name is given as one of the following types: LIBSSH2_KNOWNHOST_TYPE_PLAIN or LIBSSH2_KNOWNHOST_TYPE_CUSTOM.
        The key is encoded using one of the following encodings: LIBSSH2_KNOWNHOST_KEYENC_RAW or LIBSSH2_KNOWNHOST_KEYENC_BASE64.
        */
        val host: CPointerVar<libssh2_knownhost> = alloc()
        //TODO: fix this up so that it actually works
        val test = "AAAAC3NzaC1lZDI1NTE5AAAAIAs5CmvRp22l3kkoF9x1zQ0X0Pr3B03lt/7yEA08lRu/"
        val check = libssh2_knownhost_checkp(
          nh,
          config.hostName,
          config.port,
          test,//Base64.encode(fingerprintString),
          test.length.convert(),//Base64.encode(fingerprintString).length.convert(),
          LIBSSH2_KNOWNHOST_TYPE_PLAIN or LIBSSH2_KNOWNHOST_KEYENC_BASE64,
          host.ptr
        )
        
        log.debug {
          val checkMessage = when (check) {
            LIBSSH2_KNOWNHOST_CHECK_FAILURE -> "LIBSSH2_KNOWNHOST_CHECK_FAILURE" //3 - something prevented the check to be made
            LIBSSH2_KNOWNHOST_CHECK_NOTFOUND -> "LIBSSH2_KNOWNHOST_CHECK_NOTFOUND" //2 - no host match was found
            LIBSSH2_KNOWNHOST_CHECK_MISMATCH -> "LIBSSH2_KNOWNHOST_CHECK_MISMATCH" //1 - host was found, but the keys didn't match!
            LIBSSH2_KNOWNHOST_CHECK_MATCH -> "LIBSSH2_KNOWNHOST_CHECK_MATCH" //0 - hosts and keys match.
            else -> "UNKNOWN check value $check"
          }
          "libssh2_knownhost_checkp -> $checkMessage ($check), name:${host.pointed?.name?.toKString()} key:${host.pointed?.key?.toKString()} "
        }


      } finally {
        if (nh != null)
          libssh2_knownhost_free(nh)
      }
    }
  }


  fun connect(sessionConfig: SessionConfig): Session = Session(sessionConfig).also(Session::connect)

}
