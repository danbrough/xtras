#!/usr/bin/bash

docker run --rm --privileged -h xtras -u xtras  \
    type=bind,src=$(realpath ../),dst=/home/xtras/src \
    -w /home/xtras/src \
    -it danbrough/xtras  bash


