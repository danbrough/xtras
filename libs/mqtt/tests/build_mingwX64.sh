#!/bin/bash

cd "$(dirname "$0")"

. common.sh
echo $(pwd)

checkout_src
export INSTALL="$ROOT/install/mqtt"


function do_cmake(){
  rm -rf $BUILD $INSTALL 2> /dev/null
  mkdir -p $BUILD && mkdir -p $INSTALL
  export CFLAGS=-Wno-deprecated-declarations
  SSL=/home/dan/workspace/xtras/xtras/libs/openssl/mingwX64/3.2.1
  #mkdir $BUILD && cd $BUILD
  export LD_LIBRARY_PATH=$ROOT
  CMAKE=/usr/bin/i686-w64-mingw32-cmake
#        "-DCMAKE_TOOLCHAIN_FILE=${buildEnv.androidNdkDir.resolve("build/cmake/android.toolchain.cmake")}",
#      "-DOPENSSL_INCLUDE_DIR=${sslDir.resolve("include")}",
#      "-DOPENSSL_CRYPTO_LIBRARY=${sslDir.resolve("lib/libcrypto.so")}",
#      "-DOPENSSL_SSL_LIBRARY=${sslDir.resolve("lib/libssl.so")}",
  read -r -d '' CMD <<EOF
  $CMAKE -B "$BUILD" \
          -DCMAKE_INSTALL_PREFIX=$INSTALL  \
          -DPAHO_WITH_SSL=TRUE \
          -DPAHO_BUILD_STATIC=TRUE \
          -DPAHO_BUILD_SHARED=FALSE \
          -DPAHO_ENABLE_TESTING=FALSE \
          -DCMAKE_TOOLCHAIN_FILE=$SRC/cmake/toolchain.win64.cmake \
          -DPAHO_BUILD_SAMPLES=TRUE \
          -DPAHO_BUILD_DOCUMENTATION=FALSE \
          -DOPENSSL_ROOT_DIR=$SSL \
          -DOPENSSL_INCLUDE_DIR=$SSL/include \
          -DOPENSSL_CRYPTO_LIBRARY=$SSL/bin/libcrypto-3-x64.dll \
          -DOPENSSL_SSL_LIBRARY=$SSL/bin/libssl-3-x64.dll \
          -DOPENSSL_ROOT_DIR=$SSL \
          $SRC
EOF

  log_info running $CMD
  $CMD || exit 1
  cd "$BUILD" && make -j5 && make install
  #make -j5 && make install
}

do_cmake