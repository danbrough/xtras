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
    uint32_t hostaddr;
    struct sockaddr_in sin;
    sin.sin_family = AF_INET;
    sin.sin_port = htons(port);
    sin.sin_addr.s_addr = inet_addr(hostaddr);


    hostaddr = inet_addr(hostName);

    /* Ultra basic "connect to port 22 on localhost".  Your code is
     * responsible for creating the socket establishing the connection
     */
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock == LIBSSH2_INVALID_SOCKET) {
        fprintf(stderr, "failed to create socket!\n");
        return 0;
    }

    sin.sin_family = AF_INET;
    sin.sin_port = htons(port);
    sin.sin_addr.s_addr = hostaddr;
    if(connect(sock, (struct sockaddr*)(&sin), sizeof(struct sockaddr_in))) {
        fprintf(stderr, "failed to connect!\n");
        return 0;
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


#endif
