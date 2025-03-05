
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras2)
}

kotlin {
  linuxX64()
}

tasks.register("thang"){
  doFirst {

  }
}

