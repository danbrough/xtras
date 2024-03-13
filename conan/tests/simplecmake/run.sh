#!/usr/bin/bash

cd `dirname $0`
rm -rf build CMakeUserPresets.json 2> /dev/null


function linuxTest(){
  BUILD=build/linuxX64
  mkdir build
  conan install . --output-folder=$BUILD --build=missing
  cmake -B$BUILD -DCMAKE_TOOLCHAIN_FILE=conan_toolchain.cmake -DCMAKE_BUILD_TYPE=Release
  cmake --build $BUILD

  printf "\n### running compressor...\n\n"
  ./$BUILD/compressor
}


function androidX64Test(){
  BUILD=build/androidX64
  mkdir build
  conan install . --output-folder=$BUILD --build=missing -pr:b=default -pr:h=../profiles/androidX64 || exit 1
  cmake -B$BUILD -DCMAKE_TOOLCHAIN_FILE=conan_toolchain.cmake -DCMAKE_BUILD_TYPE=Release || exit 1
  cmake --build $BUILD
}

#linuxTest
androidX64Test

