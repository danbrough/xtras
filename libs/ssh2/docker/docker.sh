#!/bin/bash

cd "$(dirname "$0")"

source env.sh

# generate sally's key
[ ! -f sally.key ] && (ssh-keygen -t ed25519 -f sally.key -P $PASSWORD || exit 1)

# generate docker image
docker image inspect $IMAGE > /dev/null 2>&1 || (\
  echo building docker .. && \
  docker build --build-arg PASSWORD=$PASSWORD -t $IMAGE  . || exit 1)

# ssh will be available on 127.0.0.1 only
docker run --rm -d -p $BIND:$PORT:22 --name $CONTAINER $IMAGE || exit 1

cat <<HERE
Ready to login to $CONTAINER (passphrase set in env.sh):
ssh -o PasswordAuthentication=no -i sally.key -p $PORT sally@$BIND
HERE





