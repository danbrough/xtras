#!/usr/bin/env bash

cd "$(dirname "$0")"


docker buildx build . -t danbrough/xtras:latest
