import org.danbrough.xtras.konanEnvironment
import org.danbrough.xtras.tasks.scriptEnvironment
import org.danbrough.xtras.xInfo

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("org.danbrough.openssl")
}

group = "org.danbrough.openssl"

kotlin {
  linuxX64()
  linuxArm64()
  //androidNativeX64()
//  macosX64()


}




tasks.register("test") {
  doFirst {
    xInfo("konanEnv: defaultPath: ${xtras.environment.pathDefault.get()}")
    val env = scriptEnvironment()
    env["PATH"] = xtras.environment.pathDefault.get()
    xtras.environment.konanEnvironment(env)
    xInfo("env: $env")
  }
}


