#!/bin/bash

cd `dirname $0` && cd ..

PATH=/opt/kotlin/bin:$PATH
ROOT=`pwd`
DEF_FILE=$ROOT/tests/openssl_3.1.5.def
BUILD=/tmp/cinteropsTest
TARGET=android_x64

rm -rf $BUILD; mkdir $BUILD && cd $BUILD
cinterop -verbose -o openssl_$TARGET.klib -pkg org.danbrough.xtras.openssl  -def $DEF_FILE  \
  -target $TARGET


#Usage: cinterop options_list
#Options:
#    -verbose [false] -> Enable verbose logging output
#    -pkg -> place generated bindings to the package { String }
#    -output, -o [nativelib] -> specifies the resulting library file { String }
#    -libraryPath -> add a library search path { String }
#    -staticLibrary -> embed static library to the result { String }
#    -library, -l -> library to use for building { String }
#    -libraryVersion, -lv [unspecified] -> resulting interop library version { String }
#    -repo, -r -> repository to resolve dependencies { String }
#    -no-default-libs [false] -> don't link the libraries from dist/klib automatically
#    -nodefaultlibs [false] -> don't link the libraries from dist/klib automatically  Warning: Old form of flag. Please, use no-default-libs.
#    -no-endorsed-libs [false] -> don't link the endorsed libraries from dist automatically
#    -Xpurge-user-libs [false] -> don't link unused libraries even explicitly specified
#    -nopack [false] -> Don't pack the produced library into a klib file
#    -Xtemporary-files-dir -> save temporary files to the given directory { String }
#    -Xproject-dir -> base directory for relative libraryPath { String }
#    -Xkotlinc-option -> additional kotlinc compiler option { String }
#    -Xoverride-konan-properties -> Override konan.properties.values { String }
#    -Xkonan-data-dir -> Path to konan and dependencies root folder { String }
#    -target [host] -> native target to compile to { String }
#    -def -> the library definition file { String }
#    -header -> header file to produce kotlin bindings for { String }
#    -headerFilterAdditionalSearchPrefix, -hfasp -> header file to produce kotlin bindings for { String }
#    -compilerOpts -> additional compiler options (allows to add several options separated by spaces) { String } Warning: -compilerOpts is deprecated. Please use -compiler-options.
#    -compiler-options -> additional compiler options (allows to add several options separated by spaces) { String }
#    -linkerOpts -> additional linker options (allows to add several options separated by spaces) { String } Warning: -linkerOpts is deprecated. Please use -linker-options.
#    -linker-options -> additional linker options (allows to add several options separated by spaces) { String }
#    -compiler-option -> additional compiler option { String }
#    -linker-option -> additional linker option { String }
#    -linker -> use specified linker { String }
#    -Xcompile-source -> additional C/C++ sources to be compiled into resulting library { String }
#    -Xsource-compiler-option -> compiler options for sources provided via -Xcompile-source { String }
#    -Xshort-module-name -> A short name used to denote this library in the IDE { String }
#    -Xmodule-name -> A full name of the library used for dependency resolution { String }
#    -Xforeign-exception-mode -> Handle native exception in Kotlin: <terminate|objc-wrap> { String }
#    -Xdump-bridges -> Dump generated bridges
#    -Xdisable-exception-prettifier [false] -> Don't hide exceptions with user-friendly ones
#    -Xuser-setup-hint -> A suggestion that is displayed to the user if produced lib fails to link { String }
#    -Xdisable-experimental-annotation -> Don't add @ExperimentalForeignApi to generated Kotlin declarations
#    -help, -h -> Usage info

