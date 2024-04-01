
export ROOT=`pwd`
export SRC="$ROOT/src"
export BUILD="$ROOT/build"

log_info(){
        printf '\x1b[1;32m### %s\x1b[0m\n' "$*"
}

log_warn(){
        printf '\x1b[1;33m### %s\x1b[0m\n' "$*"
}


log_error(){
        printf '\x1b[1;31m### %s\x1b[0m\n' "$*"
        exit 1
}

function checkout_src() {
  log_info "checkout_src"
  [ -d $BUILD ] && rm -rf $BUILD
  if [ -d $SRC ]; then
    cd $SRC
    log_info cleaning $SRC
    git clean -xdf && git reset --hard
  else
    log_info "$SRC doesnt exist "
    git clone https://github.com/eclipse/paho.mqtt.c.git $SRC
    cd src && git checkout v1.3.13
  fi
}