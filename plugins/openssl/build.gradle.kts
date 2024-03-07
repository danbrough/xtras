import org.danbrough.xtras.XTRAS_PACKAGE

plugins {
  `kotlin-dsl`
}

group = "$XTRAS_PACKAGE.openssl"

dependencies {
  implementation(xtras.xtras.plugin)
}