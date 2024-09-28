#!/bin/bash

cd `dirname $0` && cd ..

#rm -rf xtras/maven

./gradlew  -PpluginOnly=1 -Psonatype.closeRepository=1 -Ppublish.docs=1 -Ppublish.sign=1 -Psonatype.description="xtras:plugin" \
  :plugin:publishAllPublicationsToSonatype || exit 1

#rsync -avHSx ./xtras/maven/ maven:~/.m2/

