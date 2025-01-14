#!/bin/bash

cd `dirname $0` && cd ..

#rm -rf xtras/maven

echo ./gradlew  -PpluginOnly=1 -Psonatype.closeRepository=1 -Ppublish.docs=1 -Ppublish.sign=1  $1:publishAllPublicationsToSonatype 
./gradlew  -PpluginOnly=1 -Psonatype.closeRepository=1 -Ppublish.docs=1 -Ppublish.sign=1  $1:publishAllPublicationsToSonatype || exit 1

#rsync -avHSx ./xtras/maven/ maven:~/.m2/

