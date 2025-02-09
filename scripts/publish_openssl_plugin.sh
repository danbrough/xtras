#!/bin/bash

./gradlew -Psonatype.closeRepository=1 -Psonatype.openRepository=1 -Ppublish.docs=1 -Ppublish.sign=1 \
	:openssl_plugin:publishAllPublicationsToSonatype
