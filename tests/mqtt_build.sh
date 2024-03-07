#!/bin/bash

cd `dirname $0`

mkdir -p ../tmp/mqtt_test 2> /dev/null
cd ../tmp/mqtt_test


ROOTDIR=`pwd`
SRC=/home/dan/workspace/xtras/xtras/source/mqtt/linuxX64/1.3.13
KONAN=/home/dan/.konan
LLVM=/home/dan/.konan/dependencies/llvm-11.1.0-linux-x64-essentials
NDK=/mnt/files/sdk/android/ndk/25.0.8775105
#NDK=/mnt/files/sdk/android/ndk/22.1.7171670

export DEFAULT_PATH=$PATH
export BUILD=$ROOTDIR/build


function do_cmake(){
rm -rf $BUILD $INSTALL 2> /dev/null
export CFLAGS=-Wno-deprecated-declarations
mkdir $BUILD && cd $BUILD
cmake -G "Unix Makefiles" \
        -DCMAKE_INSTALL_PREFIX=$INSTALL  \
        -DPAHO_WITH_SSL=TRUE \
        -DPAHO_BUILD_STATIC=TRUE \
        -DPAHO_BUILD_SHARED=TRUE \
        -DPAHO_ENABLE_TESTING=FALSE \
        -DPAHO_BUILD_SAMPLES=TRUE \
        -DPAHO_BUILD_DOCUMENTATION=FALSE \
        -DOPENSSL_ROOT_DIR=$SSL \
        $SRC || exit 1
make -j5 && make install
}

function do_cmake_android(){
rm -rf $BUILD $INSTALL 2> /dev/null
mkdir $BUILD && cd $BUILD
cmake -G "Unix Makefiles" \
        -DCMAKE_INSTALL_PREFIX=$INSTALL  \
        -DPAHO_WITH_SSL=TRUE \
        -DPAHO_BUILD_STATIC=TRUE \
        -DPAHO_BUILD_SHARED=TRUE \
        -DPAHO_ENABLE_TESTING=FALSE \
        -DPAHO_BUILD_SAMPLES=TRUE \
        -DPAHO_BUILD_DOCUMENTATION=FALSE \
        -DOPENSSL_ROOT_DIR=$SSL \
        -DCMAKE_TOOLCHAIN_FILE=$NDK/build/cmake/android.toolchain.cmake \
        $SRC || exit 1
  make -j5 && make install
}



function unset_vars(){
  unset SSL INSTALL CC PATH CXX AR RANLIB CFLAGS
  export PATH=$DEFAULT_PATH
}

function do_cmake_linuxarm64(){
  unset_vars
  export INSTALL=$ROOTDIR/mqtt_linuxarm64
  export SSL=/home/dan/workspace/xtras/xtras/libs/openssl/linuxArm64/3.2.1


  export CC="clang --target=aarch64-unknown-linux-gnu \
  --gcc-toolchain=$KONAN/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 \
  --sysroot=$KONAN/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
  do_cmake
}


function do_cmake_linuxx64(){
  unset_vars
  export INSTALL=$ROOTDIR/mqtt_linuxX64
  export SSL=/home/dan/workspace/xtras/xtras/libs/openssl/linuxX64/3.2.1
  export CC="clang --target=x86_64-unknown-linux-gnu \
  --gcc-toolchain=$KONAN/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 \
  --sysroot=$KONAN/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
  do_cmake
}

function do_cmake_androidX64(){
  unset_vars
  export INSTALL=$ROOTDIR/mqtt_androidX64
  export SSL=/home/dan/workspace/xtras/xtras/libs/openssl/androidNativeX64/3.2.1
  export PATH=$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH
  PREFIX=x86_64-linux-android21
  export CC=$PREFIX-clang
  export CXX=$PREFIX-clang++
  export LD=ld
  #export AR=llvm-ar
  #export RANLIB=ranlib
  export CFLAGS="-Wno-deprecated-declarations"
  do_cmake
}

#do_cmake_androidX64
do_cmake_linuxarm64
#do_cmake_linuxx64




