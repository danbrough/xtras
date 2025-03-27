import org.danbrough.xtras.xInfo

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("org.danbrough.openssl")
}

group = "org.danbrough.openssl"

kotlin {
  linuxX64()
  linuxArm64()
  androidNativeArm64()
//  macosX64()


}

xtras {

}


tasks.register("test") {
  doFirst {
    xInfo("message= ${project.property("message")}")

  }
}


