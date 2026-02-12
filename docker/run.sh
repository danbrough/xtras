#!/usr/bin/bash

cd "$(dirname "$0")"

[ -z "$CACHE" ] && echo you need to set CACHE to a cache directory && exit 1

docker run --rm --privileged -h xtras -u xtras  \
  --mount type=bind,src=$(realpath ../),dst=/home/xtras/src \
  --mount type=bind,src=${CACHE},dst=/home/xtras/cache \
  -w /home/xtras/src \
  -it danbrough/xtras  bash

#-w /home/xtras/src \



