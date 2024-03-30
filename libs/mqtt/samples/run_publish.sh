#!/usr/bin/bash


cd `dirname $0`

EXE=mqtt_publish

source common.sh


OPTS="$DEF_OPTS  -lpaho-mqtt3as"

SRC=MQTTAsync_publish.c
echo gcc $OPTS -o $EXE $SRC
gcc $OPTS -o $EXE $SRC || exit 1

run_exe

