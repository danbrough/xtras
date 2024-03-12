
#ifndef __THANG_H__
#define __THANG_H__

#include <stdint.h>

typedef uint64_t thang_counter_t;

typedef struct _thang {
    int n;
    double d;
} Thang;

const char *MESSAGE = "Hello from thang.h!";

#endif