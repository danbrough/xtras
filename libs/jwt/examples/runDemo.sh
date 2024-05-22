#!/usr/bin/bash


DEMO=$1


gcc -o demo_$DEMO demo$DEMO.c -ljwt -lssl -lcrypto -ljansson || exit 1
./demo_$DEMO


