#ifndef __SSH_SOCKETS_H__
#define  __SSH_SOCKETS_H__
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
static struct sockaddr_in ssh2_sock_address(const char *hostaddr, const int port) {
    struct sockaddr_in sin;
    sin.sin_family = AF_INET;
    sin.sin_port = htons(port);
    sin.sin_addr.s_addr = inet_addr(hostaddr);
    return sin;
}

static libssh2_socket_t ssh2_socket_connect(const char* hostName,const int port){
    libssh2_socket_t sock;
    //uint32_t hostaddr;
    struct sockaddr_in sin;
    sin.sin_family = AF_INET;

    //printf("ssh2_socket_connect() %s:%d\n",hostName,port);fflush(stdout);
    sin.sin_port = htons(port);
    sin.sin_addr.s_addr = inet_addr(hostName);



    /* Ultra basic "connect to port 22 on localhost".  Your code is
     * responsible for creating the socket establishing the connection
     */
    //printf("ssh2_socket_connect::creating socket\n");fflush(stdout);
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock == LIBSSH2_INVALID_SOCKET) {
        fprintf(stderr, "failed to create socket!\n");fflush(stderr);
        return LIBSSH2_INVALID_SOCKET;
    }

    //printf("ssh2_socket_connect::connect\n");fflush(stdout);
    if(connect(sock, (struct sockaddr*)(&sin), sizeof(struct sockaddr_in))) {
        fprintf(stderr, "failed to connect!\n");fflush(stderr);
        return LIBSSH2_INVALID_SOCKET;
    }
    return  sock;
}

static void ssh2_socket_close(libssh2_socket_t sock){
    if(sock != LIBSSH2_INVALID_SOCKET) {
        shutdown(sock, 2);
#ifdef WIN32
        closesocket(sock);
#else
        close(sock);
#endif
    }
}

//Missing from the kotlin MPP android native libraries
static inline uint32_t inetAddr(const char *cp) {
    return inet_addr(cp);
}

//Missing from darwin
static inline uint16_t ssh2_htons(uint16_t hostshort) {
    return htons(hostshort);
}




static int waitsocket(libssh2_socket_t socket_fd, LIBSSH2_SESSION *session) {
    struct timeval timeout;
    int rc;
    fd_set fd;
    fd_set *writefd = NULL;
    fd_set *readfd = NULL;
    int dir;

    timeout.tv_sec = 1;
    timeout.tv_usec = 0;

    FD_ZERO(&fd);

    FD_SET(socket_fd, &fd);

    /* now make sure we wait in the correct direction */
    dir = libssh2_session_block_directions(session);

    if (dir & LIBSSH2_SESSION_BLOCK_INBOUND)
        readfd = &fd;

    if (dir & LIBSSH2_SESSION_BLOCK_OUTBOUND)
        writefd = &fd;

    printf("calling select\n");
    fflush(stdout);
    rc = select((int) (socket_fd + 1), readfd, writefd, NULL, &timeout);
    printf("select returned %d\n",rc);
    fflush(stdout);
    return rc;
}


static LIBSSH2_API inline void libssh2_socket_close2(libssh2_socket_t socket) {
#ifdef _WIN32
    closesocket(socket);
#else /* !_WIN32 */
    close(socket);
#endif /* _WIN32 */
}



#endif
