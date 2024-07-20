#!/bin/bash

cd `dirname $0` && cd ..

./gradlew  -PpluginOnly=1 -Psonatype.closeRepository=1 -Ppublish.docs=1 \
  :plugin:publishAllPublicationsToSonatype
