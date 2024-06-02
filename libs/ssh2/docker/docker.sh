#!/bin/bash

cd "$(dirname "$0")"

source env.sh

[ ! -f sally.key ] && (ssh-keygen -t ed25519 -f sally.key -P $PASSWORD || exit 1)

docker image inspect $IMAGE > /dev/null 2>&1 || (\
  echo building docker .. && \
  docker build --build-arg PASSWORD=$PASSWORD -t $IMAGE  . || exit 1)

# stop any existing container
docker rm -f $CONTAINER > /dev/null 2>&1

# ssh will be available on 127.0.0.1 only
docker run --rm -d -p 0.0.0.0:$PORT:22 --name $CONTAINER $IMAGE || exit 1

cat <<HERE
Ready to login to $CONTAINER (passphrase set in env.sh):
ssh -o PasswordAuthentication=no -i sally.key -p $PORT sally@localhost
HERE





