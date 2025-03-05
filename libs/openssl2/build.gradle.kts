import org.danbrough.xtras.XtrasProperty.Companion.getXtrasProperty

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras2)
}

kotlin {
  linuxX64()
}

xtras2 {
  //description = "Hello World!"
}

tasks.register("thang") {
  doFirst {
    println("xtras2.message: ${getXtrasProperty<String?>("xtras2.message",null)}")
  }
}

