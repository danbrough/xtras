
export XTRAS_CACHE=/home/xtras/cache/xtras
export GRADLE_USER_HOME="$XTRAS_CACHE/gradle"
export KONAN_DATA_DIR="$XTRAS_CACHE/konan"
export NDK_VERSION=android-ndk-r27c

cp ~/src/docker/home/gradle.properties "$GRADLE_USER_HOME/"