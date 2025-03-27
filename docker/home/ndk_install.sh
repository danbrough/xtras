 #!/usr/bin/bash


 . /home/xtras/env.sh
cd "$XTRAS_CACHE"


[ -d ndk ] && exit 0

ZIP=android-ndk-r27c-linux.zip

if [ ! -f "$ZIP" ]; then
  wget https://dl.google.com/android/repository/$ZIP || exit 1
fi

unzip "$ZIP"
mv android-ndk-r27c ndk
