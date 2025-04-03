 #!/usr/bin/bash


 . /home/xtras/env.sh
cd "$XTRAS_CACHE"


[ -d "$NDK_VERSION" ] && exit 0

ZIP=$NDK_VERSION-linux.zip

if [ ! -f "$ZIP" ]; then
  wget https://dl.google.com/android/repository/$ZIP || exit 1
fi

unzip "$ZIP"


