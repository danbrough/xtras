import org.danbrough.xtras.XtrasLogger.Companion.xtrasLogger

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.xtras)
}

kotlin {
  linuxX64()
}

xtras {
  logging {
    println("log name: $name")
  }
}

tasks.register("thang") {
  doFirst {
    val log = xtrasLogger
    log.trace("logger tag trace: ${log.tag}")
    log.debug("logger tag debug: ${log.tag}")
    log.info("logger tag info: ${log.tag}")
    log.warn("logger tag warn: ${log.tag}")
    log.error("logger tag error: ${log.tag}")
    log.info("sdkVersion: ${xtras.android.sdkVersion.get()}")
  }

}

