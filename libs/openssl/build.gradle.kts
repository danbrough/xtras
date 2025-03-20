import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xError
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xWarn
import org.danbrough.xtras.xtrasBuildDir
import org.danbrough.xtras.xtrasCacheDir
import org.danbrough.xtras.xtrasDir
import org.danbrough.xtras.xtrasLogger

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("org.danbrough.openssl")
}

group = "org.danbrough.openssl"

kotlin {
  linuxX64()
  linuxArm64()
  androidNativeX64()

  openssl {
  }
}



xtras {
  logging {}
}

tasks.register("thang") {
  doFirst {
    val log = project.xtrasLogger
    xInfo("openssl.version = ${openssl.version.get()} group = ${openssl.group.get()}")
    log.trace("logger tag trace: ${log.tag}")
    xDebug("logger tag debug: ${log.tag}")
    xInfo("logger tag info: ${log.tag}")
    xWarn("logger tag warn: ${log.tag}")
    xError("logger tag error: ${log.tag}")
    xInfo("sdkVersion: ${xtras.android.sdkVersion.get()}")
    xDebug("xtrasDir: ${xtrasDir.absolutePath} downloadsDir: ${xtrasCacheDir.absolutePath} buildDir: ${xtrasBuildDir.absolutePath}")
    //val target = KonanTarget.LINUX_ARM32_HFP
    openssl.buildTargets.get().forEach {
      xInfo("NATIVE TARGET: $it name: ${it.name}")
    }
  }
}

