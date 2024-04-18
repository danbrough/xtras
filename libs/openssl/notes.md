# Building with mingwx64 on linux

This works:

AR=x86_64-w64-mingw32-ar
CC=x86_64-w64-mingw32-gcc
RANLIB=x86_64-w64-mingw32-ranlib
RC=x86_64-w64-mingw32-windres

`./Configure  mingw64 threads no-tests
--libdir=lib --prefix=/tmp/openssl --with-zlib-lib=/usr/x86_64-w64-mingw32/lib/
--with-zlib-include=/usr/x86_64-w64-mingw32/include zlib-dynamic`

## Docs mention

`./Configure mingw64 --cross-compile-prefix=x86_64-w64-mingw32- ...`


