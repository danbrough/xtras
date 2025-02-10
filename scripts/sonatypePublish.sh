#!/bin/bash

echo 'usage: OPEN=? CLOSE=? ID=? '$0'  [gradle package]'

[ ! -f gradle.properties ] && echo gradle.properties not found && exit 0

[ -z "$CLOSE" ] && CLOSE=0
[ -z "$OPEN" ] && OPEN=0

if [ -z "$ID" ]; then
	REPO=""
else
	REPO="-Psonatype.repoID=orgdanbrough-$ID"
fi

echo open $OPEN close $CLOSE REPO $REPO

CMD="./gradlew -Ppublish.docs=1 -Ppublish.sign=1 \
	-Psonatype.closeRepository=$CLOSE -Psonatype.openRepository=$OPEN \
	$REPO $@"

echo running: $CMD
$CMD




