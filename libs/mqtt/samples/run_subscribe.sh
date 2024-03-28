#!/usr/bin/bash


cd `dirname $0`

EXE=mqtt_subscribe
MQTT=/home/dan/workspace/xtras/xtras/libs/mqtt/linuxX64/1.3.13


[ -f $EXE ] && rm $EXE

OPTS="-I. -I$MQTT/include -L$MQTT/lib -lpaho-mqtt3as"

SRC=MQTTAsync_subscribe.c

echo gcc $OPTS -o $EXE $SRC
gcc $OPTS -o $EXE $SRC || exit 1

LD_LIBRARY_PATH=$MQTT/lib ./$EXE

