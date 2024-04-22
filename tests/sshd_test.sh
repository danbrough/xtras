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


function mingwX64Test(){
  if [ ! -f Makefile ]; then
    ./configure \
    --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/mingwX64/3.3.0 \
    --host=x86_64-w64-mingw32 --prefix=$INSTALL || exit 1
  fi

  make -j4  && make install && cp example/*.exe $INSTALL/bin
}

#onfigure --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/linuxArm64/3.3.0
# --host=aarch64-unknown-linux-gnu --prefix=/home/dan/workspace/xtras/xtras/build/ssh2/linuxArm64/1.11.0 --with-libz

function aarch64Test(){
  if [ ! -f Makefile ]; then
    CC=clang
    DEPSDIR=/home/dan/.konan/dependencies
    export CLANG_ARGS="--target=aarch64-unknown-linux-gnu --gcc-toolchain=$DEPSDIR/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$DEPSDIR/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
    export PATH=/home/dan/.konan/dependencies/llvm-11.1.0-linux-x64-essentials/bin:$PATH
    export CC="clang $CLANG_ARGS"
    export CXX="clang++ $CLANG_ARGS"

    ./configure \
        --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/linuxArm64/3.3.0 \
        --host=aarch64-unknown-linux-gnu --prefix=$INSTALL --with-libz || exit 1
    mkdir -p $INSTALL/bin
    make -j4 && make install and cp/example/.libs/* $INSTALL/bin
  fi
}

mingwX64Test






