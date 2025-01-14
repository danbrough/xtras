package demo


import kotlinx.cinterop.ExperimentalForeignApi
import org.danbrough.jni.cinterops.jlong
import kotlin.experimental.ExperimentalNativeApi

val log = klog.logger("DEMO")

@OptIn(ExperimentalNativeApi::class)
private fun testLog() {
  log.trace { "log trace" }
  log.debug { "log debug" }
  log.info { "log info" }
  log.warn { "log warn" }
  val err = Exception("Demo Exception")
  log.error(err) {
    "example error message: ${err.getStackTrace().joinToString("\n")}"
  }
}

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
  println("Hello from the demo")

  testLog()
  val t: jlong = 0L
  println("t is $t")

}