package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_PACKET_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_WINDOW_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
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
import org.danbrough.ssh2.cinterops.LIBSSH2_KNOWNHOST_TYPE_PLAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.cinterops.libssh2_channel_open_ex
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
import org.danbrough.ssh2.cinterops.libssh2_session_last_errno
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_socket_close2
import org.danbrough.ssh2.cinterops.libssh2_userauth_publickey_fromfile_ex
import org.danbrough.ssh2.cinterops.ssh2_sock_address
import org.danbrough.ssh2.cinterops.waitsocket
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.connect
import platform.posix.shutdown
import platform.posix.size_tVar
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror
import kotlin.io.encoding.Base64

class SessionNative internal constructor(@Suppress("MemberVisibilityCanBePrivate") val config: SessionConfig) :
  AutoCloseable {
  private var sock: SshSocket = 0
  private var session: CPointer<LIBSSH2_SESSION>? = null

  internal fun connect() {
    memScoped {
      log.info { "SSH.connect() ${config.user}@${config.hostName}:${config.port}" }

      sock = socket(AF_INET, SOCK_STREAM, 0).convert()
      if (sock == LIBSSH2_INVALID_SOCKET.toLong())
        error("Failed to create socket")
      log.trace { "created socket" }

      /*val sockAddress = cValue<sockaddr_in>() {
        sin_family = AF_INET.convert()
        sin_port = org.danbrough.ssh2.cinterops.ssh2_htons(config.port.convert())
        sin_addr.s_addr = org.danbrough.ssh2.cinterops.inetAddr(config.hostName)
      }*/

      val sockAddress = ssh2_sock_address(config.hostName, config.port)


      connect(sock.convert(), sockAddress.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
        .also {
          log.trace { "connected returned $it" }
          if (it != 0)
            error("Failed to connect: ${strerror(it)?.toKString()}")
        }

      log.trace { "socket connected" }

      session =
        libssh2_session_init_ex(null, null, null, null)
          ?: error("Failed to created ssh session")

      libssh2_session_set_blocking(session, 0)

      /* Enable all debugging when libssh2 was built with debugging enabled */
      //libssh2_trace(session, 0)

      var rc = 0
      do {
        rc = libssh2_session_handshake(session, sock.convert())
      } while (rc == LIBSSH2_ERROR_EAGAIN)
      if (rc != 0) error("libssh2_session_handshake(session, sock) failed. returned: $rc")
      log.debug { "handshake complete" }

      config.knownHostsFile?.also { loadKnownHosts(this, it) }

      if (!authenticate()) error("Failed to authenticate")


    }

  }


  fun waitSocket() {
    waitsocket(sock.convert(), session)
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

  private fun authenticate(): Boolean {

    log.info { "authenticate() authMethod:${config.authMethod}" }

    /*var userAuthList: String?
    var rc = 0
    do {
      userAuthList = libssh2_userauth_list(session, config.user, config.user.length.convert())?.toKString()
      if (userAuthList == null)
        rc = libssh2_session_last_errno(session)
    } while (userAuthList == null && rc == LIBSSH2_ERROR_EAGAIN)

    if (userAuthList == null) error("libssh2_userauth_list failed with error: $rc")

    log.debug { "userAuthList: $userAuthList rc: $rc" }
    val authMethodsSupported = userAuthList.split(',').toMutableList()
    val authMethods = mutableListOf<SessionConfig.AuthMethod>()*/

    val authMethods = mutableListOf<SessionConfig.AuthMethod>()
    config.authMethod?.also { authMethods.add(it) }
    config.privateKeyFile?.also {
      if (!authMethods.contains(SessionConfig.AuthMethod.KEY)) authMethods.add(SessionConfig.AuthMethod.KEY)
    }
    config.password?.also {
      if (!authMethods.contains(SessionConfig.AuthMethod.PASSWORD)) authMethods.add(SessionConfig.AuthMethod.PASSWORD)
    }

    authMethods.forEach { authMethod ->
      when (authMethod) {
        SessionConfig.AuthMethod.KEY -> authenticatePublicKey()

        SessionConfig.AuthMethod.PASSWORD -> authenticatePassword()
        else -> error("AuthMethod.KEYBOARD not supported")
      }.also {
        if (it) return true
      }
    }

    log.warn { "failed to authenticate" }
    return false
  }

  private fun authenticatePublicKey(): Boolean {
    log.info { "authenticatePublicKey()" }

    var rc = 0
    do {
      rc = libssh2_userauth_publickey_fromfile_ex(
        session, config.user, config.user.length.convert(),
        config.publicKeyFile!!, config.privateKeyFile!!, config.password!!
      )
    } while (rc == LIBSSH2_ERROR_EAGAIN)

    log.debug { "libssh2_userauth_publickey_fromfile_ex returned $rc" }
    return rc == 0
  }

  private fun authenticatePassword(): Boolean {
    log.info { "authenticatePassword()" }
    return false
  }

  fun openChannel(): Channel {
    var rc: Int
    var channel: CPointer<LIBSSH2_CHANNEL>?
    val channelType =
      "session" // Channel type to open. Typically one of session, direct-tcpip, or tcpip-forward.
    while (true) {
      /*
session - Session instance as returned by libssh2_session_init_ex
channel_type - Channel type to open. Typically one of session, direct-tcpip, or tcpip-forward. The SSH2 protocol allowed for additional types including local, custom channel types.
channel_type_len - Length of channel_type
window_size - Maximum amount of unacknowledged data remote host is allowed to send before receiving an SSH_MSG_CHANNEL_WINDOW_ADJUST packet.
packet_size - Maximum number of bytes remote host is allowed to send in a single SSH_MSG_CHANNEL_DATA or SSG_MSG_CHANNEL_EXTENDED_DATA packet.
message - Additional data as required by the selected channel_type.
message_len - Length of message parameter.
Allocate a new channel for exchanging data with the server.
This method is typically called through its macroized form: libssh2_channel_open_session
or via libssh2_channel_direct_tcpip or libssh2_channel_forward_listen
       */
      channel = libssh2_channel_open_ex(
        session,
        channelType,
        channelType.length.convert(),
        LIBSSH2_CHANNEL_WINDOW_DEFAULT.convert(),
        LIBSSH2_CHANNEL_PACKET_DEFAULT.convert(),
        null,
        0.convert()
      )

      if (channel != null) return Channel(this, channel)
      rc = libssh2_session_last_errno(session)
      if (rc != LIBSSH2_ERROR_EAGAIN) break
      waitSocket()
    }
    error("Failed to open channel: rc:$rc")
  }

  override fun close() {
    log.debug { "Session::close()" }
    /*
        if(session) {
        libssh2_session_disconnect(session, "Normal Shutdown");
        libssh2_session_free(session);
    }

    if(sock != LIBSSH2_INVALID_SOCKET) {
        shutdown(sock, 2);
        LIBSSH2_SOCKET_CLOSE(sock);
    }
#define libssh2_session_disconnect(session, description) \
    libssh2_session_disconnect_ex((session), SSH_DISCONNECT_BY_APPLICATION, \
                                  (description), "")

     */


    libssh2_session_disconnect_ex(session, SSH_DISCONNECT_BY_APPLICATION, "Normal Shutdown", "")
    libssh2_session_free(session)
    log.trace { "closed session" }

    if (sock != LIBSSH2_INVALID_SOCKET.toLong()) {
      shutdown(sock.convert(), 2)
      libssh2_socket_close2(sock.convert())
      log.trace { "closed socket" }
    }
  }

}
