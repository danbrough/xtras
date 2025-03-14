import org.danbrough.xtras.xDebug
import org.danbrough.xtras.xError
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xWarn
import org.danbrough.xtras.xtrasDir
import org.danbrough.xtras.xtrasLogger
import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
  id("org.danbrough.openssl")
}

group = "org.danbrough.openssl"

kotlin {
  linuxX64()
}

openssl {
}

xtras {
  logging {
  }
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
    xDebug("xtrasDir: ${xtrasDir.absolutePath}")
    println("MESSAGE: ${project.extraProperties["message"]}")
  }

}

