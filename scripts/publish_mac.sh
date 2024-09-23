PACKAGE=:$1
./gradlew -Psonatype.repoID=orgdanbrough-1783 -Psonatype.closeRepository=0 \
$PACKAGE:publishMacosArm64PublicationToSonatypeRepository \
$PACKAGE:publishMacosX64PublicationToSonatypeRepository \
$PACKAGE:publishOpensslBinariesMacosArm64PublicationToSonatypeRepository \
$PACKAGE:publishOpensslBinariesMacosX64PublicationToSonatypeRepository 
