#!/usr/bin/bash

cd `dirname $0`
PWD=`pwd`

URL=${1:-https://danbrough.org}
rm -rf build CMakeUserPresets.json 2> /dev/null

function hostTest() {
  conan install . --output-folder=build --build=missing || exit 1
  cd build
  cmake .. -DCMAKE_TOOLCHAIN_FILE=conan_toolchain.cmake -DCMAKE_BUILD_TYPE=Release
  cmake --build .
  printf "\n### running simplecurl...\n\n"
  ./simplecurl $URL
}

function androidTest(){
  conan install . --output-folder=build --build=missing -pr:b=default -pr:h=../profiles/androidX64 || exit 1
  cd build
  cmake .. -DCMAKE_TOOLCHAIN_FILE=conan_toolchain.cmake -DCMAKE_BUILD_TYPE=Release
  cmake --build .
}

androidTest







