 #!/usr/bin/bash


 . /home/xtras/env.sh
cd "$XTRAS_CACHE"


[ -d ndk ] && exit 0
NDK_VERSION=android-ndk-r27c
ZIP=$NDK_VERSION-linux.zip

if [ ! -f "$ZIP" ]; then
  wget https://dl.google.com/android/repository/$ZIP || exit 1
fi

unzip "$ZIP"

