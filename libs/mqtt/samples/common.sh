
MQTT=/home/dan/workspace/xtras/xtras/libs/mqtt/linuxX64/1.3.13
SSL=/home/dan/workspace/xtras/xtras/libs/openssl/linuxX64/3.2.1

[ ! -z $EXE ] && [ -f $EXE ] && echo removing $EXE && rm $EXE

export LD_LIBRARY_PATH=$SSL/lib:$MQTT/lib

DEF_OPTS="-I. -I$MQTT/include -L$MQTT/lib -L$SSL/lib"

function run_exe() {
  ./$EXE
}


