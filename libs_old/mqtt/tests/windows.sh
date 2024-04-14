#cmake -G "Unix Makefiles"
cmake -G "Unix Makefiles" -B /C/workspace/xtras/libs/mqtt/tests/build \
-DCMAKE_INSTALL_PREFIX=/C/xtras/build/mqtt/mingwX64/1.3.13 -DPAHO_WITH_SSL=TRUE \
-DPAHO_BUILD_STATIC=TRUE \
-DPAHO_BUILD_SHARED=TRUE \
-DPAHO_ENABLE_TESTING=FALSE \
-DPAHO_BUILD_SAMPLES=TRUE \
-DPAHO_BUILD_DOCUMENTATION=FALSE \
-DOPENSSL_ROOT_DIR=/C/xtras/libs/openssl/mingwX64/3.2.1 \
-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc \
-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++ \
-DCMAKE_SYSTEM_NAME=Windows \
-DCMAKE_SYSTEM_VERSION=1 \
-DOPENSSL_CRYPTO_LIBRARY=/C/xtras/libs/openssl/mingwX64/3.2.1/lib/libcrypto.a \
-DOPENSSL_SSL_LIBRARY=/C/xtras/libs/openssl/mingwX64/3.2.1/lib/libssl.a \
/C/xtras/source/mqtt/mingwX64/1.3.13

#cmake  -DCMAKE_INSTALL_PREFIX=C:\xtras\build\mqtt\mingwX64\1.3.13 -DPAHO_WITH_SSL=TRUE \
#-DPAHO_BUILD_STATIC=TRUE \
#-DPAHO_BUILD_SHARED=FALSE \
#-DPAHO_ENABLE_TESTING=FALSE \
#-DPAHO_BUILD_SAMPLES=FALSE \
#-DPAHO_BUILD_DOCUMENTATION=FALSE \
#-DOPENSSL_ROOT_DIR=C:\xtras\libs\openssl\mingwX64\3.2.1 \
#-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc \
#-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++ \
#-DCMAKE_SYSTEM_NAME=Windows \
#-DCMAKE_SYSTEM_VERSION=1 \
#-DOPENSSL_CRYPTO_LIBRARY=C:\xtras\libs\openssl\mingwX64\3.2.1\lib\libcrypto.a \
#-DOPENSSL_SSL_LIBRARY=C:\xtras\libs\openssl\mingwX64\3.2.1\lib\libssl.a \
#C:\xtras\source\mqtt\mingwX64\1.3.13
