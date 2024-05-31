package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_PACKET_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_CHANNEL_WINDOW_DEFAULT
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_AUTHENTICATION_FAILED
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_EAGAIN
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_FILE
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_KEYFILE_AUTH_FAILED
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_PUBLICKEY_UNVERIFIED
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_SOCKET_SEND
import org.danbrough.ssh2.cinterops.LIBSSH2_ERROR_SOCKET_TIMEOUT
import org.danbrough.ssh2.cinterops.LIBSSH2_INVALID_SOCKET
import org.danbrough.ssh2.cinterops.LIBSSH2_SESSION
import org.danbrough.ssh2.cinterops.SSH_DISCONNECT_BY_APPLICATION
import org.danbrough.ssh2.cinterops.libssh2_channel_open_ex
import org.danbrough.ssh2.cinterops.libssh2_session_disconnect_ex
import org.danbrough.ssh2.cinterops.libssh2_session_free
import org.danbrough.ssh2.cinterops.libssh2_session_handshake
import org.danbrough.ssh2.cinterops.libssh2_session_init_ex
import org.danbrough.ssh2.cinterops.libssh2_session_last_errno
import org.danbrough.ssh2.cinterops.libssh2_session_set_blocking
import org.danbrough.ssh2.cinterops.libssh2_socket_t
import org.danbrough.ssh2.cinterops.libssh2_userauth_publickey
import org.danbrough.ssh2.cinterops.libssh2_userauth_publickey_fromfile_ex
import org.danbrough.ssh2.cinterops.ssh2_socket_close
import org.danbrough.ssh2.cinterops.ssh2_socket_connect
import org.danbrough.ssh2.cinterops.waitsocket
import org.danbrough.xtras.support.getEnv

class SSHSession( val ssh: SSHScope) : Scope {
	private var sock: libssh2_socket_t = LIBSSH2_INVALID_SOCKET
	private var session: CPointer<LIBSSH2_SESSION>? = null


	fun connect(hostName: String, port: Int) {
		log.debug { "connect(): ${hostName}:$port" }
		sock = ssh2_socket_connect(hostName, port)
		if (sock == LIBSSH2_INVALID_SOCKET) error("ssh2_socket_connect ${hostName}:$port failed")

		session =
			libssh2_session_init_ex(null, null, null, null)
				?: error("Failed to created ssh session")

		libssh2_session_set_blocking(session, 0)
		//libssh2_trace(session, 0)

		var rc = 0
		do {
			rc = libssh2_session_handshake(session, sock)
		} while (rc == LIBSSH2_ERROR_EAGAIN)
		if (rc != 0) error("libssh2_session_handshake(session, sock) failed. returned: $rc")
	}

	fun authenticate(
		user: String = getEnv("USER") ?: error("user not specified"),
		publicKeyPath: String? = null,
		privateKeyPath: String? = null,
		password: String? = null
	) {
		log.debug { "authenticatePublicKey()" }

		if (session == null) error("session is null")

		/*
		config.user, config.user.length.convert(),
		 */
		var rc = 0
		do {
			rc = libssh2_userauth_publickey_fromfile_ex(
				session,
				user,
				user.length.convert(),
				publicKeyPath,
				privateKeyPath,
				password
			)
		} while (rc == LIBSSH2_ERROR_EAGAIN)

		log.debug { "libssh2_userauth_publickey_fromfile_ex returned $rc" }
		/*
		LIBSSH2_ERROR_ALLOC - An internal memory allocation call failed.

LIBSSH2_ERROR_SOCKET_SEND - Unable to send data on socket.

LIBSSH2_ERROR_SOCKET_TIMEOUT -

LIBSSH2_ERROR_PUBLICKEY_UNVERIFIED - The username/public key combination was invalid.

LIBSSH2_ERROR_AUTHENTICATION_FAILED -
		 */
		val message = when (rc) {
			0 -> "SUCCESS"
			LIBSSH2_ERROR_AUTHENTICATION_FAILED -> "LIBSSH2_ERROR_AUTHENTICATION_FAILED"
			LIBSSH2_ERROR_SOCKET_TIMEOUT -> "LIBSSH2_ERROR_SOCKET_TIMEOUT"
			LIBSSH2_ERROR_PUBLICKEY_UNVERIFIED -> "LIBSSH2_ERROR_PUBLICKEY_UNVERIFIED"
			LIBSSH2_ERROR_SOCKET_SEND -> "LIBSSH2_ERROR_SOCKET_SEND"
			LIBSSH2_ERROR_KEYFILE_AUTH_FAILED -> "LIBSSH2_ERROR_KEYFILE_AUTH_FAILED"
			LIBSSH2_ERROR_FILE -> "LIBSSH2_ERROR_FILE"
			else -> "UNKNOWN ERROR"
		}

		if (rc != 0) log.error { "authenticate failed: $message" }

		/*
int libssh2_userauth_publickey_fromfile_ex(LIBSSH2_SESSION *session,
                                           const char *username,
                                           unsigned int ousername_len,
                                           const char *publickey,
                                           const char *privatekey,
                                           const char *passphrase);
DESCRIPTION
session - Session instance as returned by libssh2_session_init_ex
username - Pointer to user name to authenticate as.
username_len - Length of username.
publickey - Path name of the public key file. (e.g. /etc/ssh/hostkey.pub). If libssh2 is built against OpenSSL, this option can be set to NULL.
privatekey - Path name of the private key file. (e.g. /etc/ssh/hostkey)
passphrase - Passphrase to use when decoding privatekey.
Attempt public key authentication using a PEM encoded private key file stored on disk

			var rc = 0
			do {
				rc = libssh2_userauth_publickey_fromfile_ex(
					session, user, user.length.convert(),
					null, confiprivateKeyFile!!, config.password!!
				)
			} while (rc == LIBSSH2_ERROR_EAGAIN)

			log.debug { "libssh2_userauth_publickey_fromfile_ex returned $rc" }
			return rc == 0
*/
	}


	/*
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
 */


	suspend fun <R> channel(block: suspend SSHChannel.() -> R) = sshScope(block) {
		var channel: CPointer<LIBSSH2_CHANNEL>? = null

		val type = "session"

		var rc = LIBSSH2_ERROR_EAGAIN
		while (true) {
			channel = libssh2_channel_open_ex(
				session!!,
				type,
				type.length.convert(),
				LIBSSH2_CHANNEL_WINDOW_DEFAULT.convert(),
				LIBSSH2_CHANNEL_PACKET_DEFAULT.convert(),
				null,
				0.convert()
			)

			if (channel != null) break
			rc = libssh2_session_last_errno(session!!)
			if (rc != LIBSSH2_ERROR_EAGAIN) break
			waitSocket()
		}

		if (channel == null) error("libssh2_channel_open_ex(type=$type) -> $rc")

		SSHChannel(this, channel)
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

	fun waitSocket() = waitsocket(sock,session)


}