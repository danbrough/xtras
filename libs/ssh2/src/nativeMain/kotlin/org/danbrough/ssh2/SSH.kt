@file:OptIn(ExperimentalForeignApi::class)

package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init
import org.danbrough.ssh2.cinterops.libssh2_session_handshake
import org.danbrough.ssh2.cinterops.libssh2_session_init_ex
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_socket_t
import org.danbrough.ssh2.cinterops.libssh2_trace
import org.danbrough.ssh2.cinterops.ssh2_exit
import org.danbrough.ssh2.cinterops.ssh2_init
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.connect
import platform.posix.htons
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror

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

        if (config.knownHostsFile != null)
          loadKnownHosts(this)
      }
    }

    private fun loadKnownHosts(memScope: MemScope) = memScope.apply {
      log.info { "loadKnownHosts(): ${config.knownHostsFile}" }

    }
  }


  fun connect(sessionConfig: SessionConfig): Session = Session(sessionConfig).also(Session::connect)

}
