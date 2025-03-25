#!/usr/bin/env bash

cd "$(dirname "$0")"


docker buildx . -t danbrough/xtras:latest
