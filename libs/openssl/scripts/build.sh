#!/bin/bash

# Test build script for openssl

GIT_URL=https://github.com/openssl/openssl.git
GIT_COMMIT=openssl-3.3.1

cd "$(dirname "$0")"

[ ! -d build ] && mkdir build
cd build

if [ ! -d src ]; then
    git clone $GIT_URL src
fi
#pushd src > /dev/null
#git clean -xdf && git checkout "$GIT_COMMIT"
#popd







