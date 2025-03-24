import org.danbrough.xtras.tasks.ScriptTask
import org.danbrough.xtras.tasks.konanEnvironment
import org.danbrough.xtras.tasks.scriptEnvironment
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
  dependsOn("xtrasOpensslSourceConfigureLinuxX64")
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


tasks.register<ScriptTask>("foo") {
//  target = KonanTarget.LINUX_ARM64
//  dependsOn(openssl.taskNameSourceExtract(target))
  workingDir("/tmp")


  script {
    println("echo the date is `date` and the message \$MESSAGE")
  }


  doFirst {
    xWarn("TEST: doFirst")
    environment.clear()
    environment["MESSAGE"] = "Set from gradle"
  }
}

tasks.register("test") {
  doFirst {
    xInfo("konanEnv: defaultPath: ${xtras.environment.pathDefault.get()}")
    val env = scriptEnvironment()
    xtras.environment.konanEnvironment(env)
    xInfo("env: $env")
  }
}


