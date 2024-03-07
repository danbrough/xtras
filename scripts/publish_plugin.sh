#!/bin/bash

cd `dirname $0` && cd ..

./gradlew  -Psonatype.repoID="sonatypeRepoId_not_specified" \
  :plugin:publishAllPublicationsToXtras

