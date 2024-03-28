#!/usr/bin/bash


cd `dirname $0`

EXE=mqtt_subscribe
MQTT=/home/dan/workspace/xtras/xtras/libs/mqtt/linuxX64/1.3.13


[ -f $EXE ] && rm $EXE

#gcc program.o -llib1 -Wl,-Bstatic -llib2 -Wl,-Bdynamic -llib3

OPTS="-I. -I$MQTT/include -L$MQTT/lib -Wl,-Bstatic -llib2 -lpaho-mqtt3as"
export LD_LIBRARY_PATH=$MQTT/lib
SRC=MQTTAsync_subscribe.c

echo gcc $OPTS -o $EXE $SRC
gcc $OPTS -o $EXE $SRC || exit 1

./$EXE

