
#ifndef __THANG_H__
#define __THANG_H__

#include <sys/types.h>

typedef u_int64_t thang_counter_t;

typedef struct _thang {
    int n;
    double d;
} Thang;

const char* MESSAGE = "Hello from thang.h!";

#endif