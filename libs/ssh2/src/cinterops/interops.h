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

#include "ssh_sockets.h"


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

