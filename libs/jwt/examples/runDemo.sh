#!/usr/bin/bash

cd "$(dirname "$0")"

DEMO=$1

rm demo_$DEMO 2> /dev/null
gcc -o demo_$DEMO demo$DEMO.c -ljwt -lssl -lcrypto -ljansson || exit 1
./demo_$DEMO


