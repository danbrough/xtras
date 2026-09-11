
# 1. Point to your REAL NDK path (e.g., /home/user/Android/Sdk/ndk/25.1.8937393)
export ANDROID_NDK="/files/sdk/android/ndk/29.0.14206865/"
export OPENSSL_DIR="/files/cache/xtras/lib/openssl_androidArm64_3.6.3"

export API=21
export TOOLCHAIN="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64"
export TARGET="aarch64-linux-android"

# 2. Assign compilers
export CC="$TOOLCHAIN/bin/${TARGET}${API}-clang"
export CXX="$TOOLCHAIN/bin/${TARGET}${API}-clang++"
export AR="$TOOLCHAIN/bin/llvm-ar"
export RANLIB="$TOOLCHAIN/bin/llvm-ranlib"

# 3. Clean and minimal flags (Removed -pthread and -lz to prevent basic link failures)
export CPPFLAGS="-I${OPENSSL_DIR}/include"
export LDFLAGS="-L${OPENSSL_DIR}/lib -L${OPENSSL_DIR}/lib64"
export LIBS="-ldl"

# 4. Clear environment pollution
unset PKG_CONFIG_LIBDIR
unset PKG_CONFIG_PATH

# 5. Run configure again
./configure \
  --prefix="/files/cache/xtras/build/ssh2_androidArm64_libssh2-1.11.1" \
  --host=aarch64-linux-android \
  --with-crypto=openssl \
  --with-libssl-prefix="${OPENSSL_DIR}" \
  --enable-static=yes \
  --enable-shared=yes


