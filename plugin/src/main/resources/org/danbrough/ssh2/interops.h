#include <libssh2.h>
#include <stddef.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>

static int waitsocket(libssh2_socket_t socket_fd, LIBSSH2_SESSION *session) {
  struct timeval timeout;
  int rc;
  fd_set fd;
  fd_set *writefd = NULL;
  fd_set *readfd = NULL;
  int dir;

  timeout.tv_sec = 10;
  timeout.tv_usec = 0;

  FD_ZERO(&fd);

  FD_SET(socket_fd, &fd);

  /* now make sure we wait in the correct direction */
  dir = libssh2_session_block_directions(session);

  if (dir & LIBSSH2_SESSION_BLOCK_INBOUND)
    readfd = &fd;

  if (dir & LIBSSH2_SESSION_BLOCK_OUTBOUND)
    writefd = &fd;

  rc = select((int) (socket_fd + 1), readfd, writefd, NULL, &timeout);

  return rc;
}

static LIBSSH2_API int libssh2_channel_exec2(LIBSSH2_CHANNEL *channel, const char *command) {
  return libssh2_channel_process_startup(channel, "exec", sizeof("exec") - 1, command,
                                         (unsigned int) strlen(command));
}


static LIBSSH2_API inline void libssh2_socket_close2(libssh2_socket_t socket) {
  LIBSSH2_SOCKET_CLOSE(socket);
}