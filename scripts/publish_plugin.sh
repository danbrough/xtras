#!/bin/bash

cd `dirname $0` && cd ..

#rm -rf xtras/maven

./gradlew  -PpluginOnly=1 :plugin:publishAllPublicationsToXtras || exit 1

rsync -avHSx /files/cache/xtras/maven/org/danbrough/xtras/  maven:~/m2/org/danbrough/xtras/

#rsync -avHSx /files/cache/xtras/maven/ maven:~/.m2/

