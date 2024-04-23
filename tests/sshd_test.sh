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

cd $SRC && make distclean


function mingwX64Test(){
    ./configure \
    --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/mingwX64/3.3.0 \
    --host=x86_64-w64-mingw32 --prefix=$INSTALL || exit 1


  make -j4  && make install && cp example/*.exe $INSTALL/bin
}

#onfigure --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/linuxArm64/3.3.0
# --host=aarch64-unknown-linux-gnu --prefix=/home/dan/workspace/xtras/xtras/build/ssh2/linuxArm64/1.11.0 --with-libz

function aarch64Test(){
    DEPSDIR=/home/dan/.konan/dependencies
    export CLANG_ARGS="--target=aarch64-unknown-linux-gnu --gcc-toolchain=$DEPSDIR/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$DEPSDIR/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
    export PATH=/home/dan/.konan/dependencies/llvm-11.1.0-linux-x64-essentials/bin:$PATH
    export CC="clang $CLANG_ARGS"
    export CXX="clang++ $CLANG_ARGS"

    ./configure \
        --host=aarch64-unknown-linux-gnu \
        --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/3.3.0/linuxArm64 \
        --prefix=$INSTALL --with-libz || exit 1
    mkdir -p $INSTALL/bin
    make -j4 && make install and cp/example/.libs/* $INSTALL/bin

}

function linuxX64Test(){

#  h ./configure --host=x86_64-unknown-linux-gnu --prefix=/home/dan/workspace/xtras/libs/ssh2/build/xtras/build/ssh2/1.11.0/linuxX64 --with-libz --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/3.3.0/linuxX64
#TRACE:       CONFIGURE: environment: {
# PATH=/home/dan/.konan/dependencies/llvm-11.1.0-linux-x64-essentials/bin:/bin:/usr/bin:/usr/local/bin,
# CXX=clang++ --target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot, MAKEFLAGS=-j6, CC=clang --target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot, CLANG_ARGS=--target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot, HOME=/home/dan}


    DEPSDIR=/home/dan/.konan/dependencies
    export CLANG_ARGS="--target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
    export PATH=/home/dan/.konan/dependencies/llvm-11.1.0-linux-x64-essentials/bin:$PATH
    export CC="clang $CLANG_ARGS"
    export CXX="clang++ $CLANG_ARGS"
    ./configure --host=x86_64-unknown-linux-gnu --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/3.3.0/linuxX64 \
            --with-libz \
            --prefix=$INSTALL  || exit 1
        mkdir -p $INSTALL/bin
        make -j4 && make install and cp/example/.libs/* $INSTALL/bin
}

function androidNativeArm64Test(){

#  h ./configure --host=x86_64-unknown-linux-gnu --prefix=/home/dan/workspace/xtras/libs/ssh2/build/xtras/build/ssh2/1.11.0/linuxX64 --with-libz --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/3.3.0/linuxX64
#TRACE:       CONFIGURE: environment: {
# PATH=/home/dan/.konan/dependencies/llvm-11.1.0-linux-x64-essentials/bin:/bin:/usr/bin:/usr/local/bin,
# CXX=clang++ --target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot, MAKEFLAGS=-j6, CC=clang --target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot, CLANG_ARGS=--target=x86_64-unknown-linux-gnu --gcc-toolchain=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=/home/dan/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot, HOME=/home/dan}

    NDK=/mnt/files/sdk/android/ndk/26.3.11579264
    DEPSDIR=/home/dan/.konan/dependencies
    export PATH=$NDK/prebuilt/linux-x86_64/bin:$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH
    export CC=clang
    export CXX=clang++
    export PREFIX=aarch64-linux-android21-
    export CC=${PREFIX}clang
    export CXX=${PREFIX}clang++
    ./configure --host=aarch64-linux-android  \
        --with-libssl-prefix=/home/dan/workspace/xtras/xtras/libs/openssl/3.3.0/androidNativeArm64 \
            --with-libz --prefix=$INSTALL  || exit 1
    mkdir -p $INSTALL/bin
    make -j4 && make install && cp example/.libs/* $INSTALL/bin
}


#mingwX64Test
#aarch64Test
#linuxX64Test
androidNativeArm64Test








