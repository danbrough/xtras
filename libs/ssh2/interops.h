#include <libssh2.h>
#include <stddef.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdio.h>
#include <sys/types.h>

#ifndef _WIN32
#include <arpa/inet.h>
#include <sys/socket.h>
#endif




static int ssh2_init(int flags) {
    int rc = 0;
#ifdef _WIN32
    WSADATA wsadata;

    rc = WSAStartup(MAKEWORD(2, 0), &wsadata);
    if(rc) {
        fprintf(stderr, "WSAStartup failed with error: %d\n", rc);
        return 1;
    }
#endif

    return libssh2_init(flags);
}

static int ssh2_exit() {
    libssh2_exit();
#ifdef _WIN32
    WSACleanup();
#endif
}

//Missing from the kotlin MPP android native libraries
static inline uint32_t inetAddr(const char *cp){
    return inet_addr(cp);
}

//Missing from darwin
static inline uint16_t ssh2_htons(uint16_t hostshort){
    return htons(hostshort);
}

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



static LIBSSH2_API inline void libssh2_socket_close2(libssh2_socket_t socket) {
#ifdef _WIN32
    closesocket(socket);
#else /* !_WIN32 */
    close(socket);
#endif /* _WIN32 */
}

