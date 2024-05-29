package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.cinterops.libssh2_session_disconnect_ex
import org.danbrough.ssh2.cinterops.libssh2_session_free
import org.danbrough.ssh2.cinterops.libssh2_socket_t
import org.danbrough.ssh2.cinterops.ssh2_socket_close

class SSHSession(private val ssh: SSHScope) : Scope {
	private var sock: libssh2_socket_t = LIBSSH2_INVALID_SOCKET
	private var session: CPointer<LIBSSH2_SESSION>? = null


	fun connect(user: String, hostName: String, port: Int) {
		log.debug { "connect(): ${user}@${hostName}:$port" }
	}

	override fun release() {
		log.trace { "SSHSession::release()" }
		if (session != null) {
			libssh2_session_disconnect_ex(session, SSH_DISCONNECT_BY_APPLICATION, "Normal Shutdown", "")
			libssh2_session_free(session)
			log.trace { "closed session" }
		}

		if (sock != LIBSSH2_INVALID_SOCKET) {
			log.trace { "SSHSession::closing socket" }
			ssh2_socket_close(sock)
		}
	}
}