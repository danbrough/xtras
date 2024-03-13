#!/usr/bin/bash

cd `dirname $0`
rm -rf build CMakeUserPresets.json 2> /dev/null


conan install . --output-folder=build --build=missing
cd build
cmake .. -DCMAKE_TOOLCHAIN_FILE=conan_toolchain.cmake -DCMAKE_BUILD_TYPE=Release
cmake --build .

printf "\n### running compressor...\n\n"
./compressor
