#!/bin/bash

cd `dirname $0`

ROOT="$(pwd)"
SRC="$ROOT/build/ssh_src"
INSTALL="$ROOT/build/ssh2"



[ ! -d build ] && mkdir build


if [ ! -d "$SRC" ]; then
  git clone ../xtras/downloads/ssh2/ $SRC
  cd $SRC && autoreconf -fi
fi

cd $SRC
if [ ! -f Makefile ]; then
  ./configure \
  --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/mingwX64/3.3.0 \
  --host=x86_64-w64-mingw32 --prefix=$INSTALL || exit 1
fi

make -j4  && make install && cp example/*.exe $INSTALL/bin







