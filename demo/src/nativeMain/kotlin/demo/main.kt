package demo

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KLogger

import org.danbrough.xtras.support.initLogging
import kotlin.experimental.ExperimentalNativeApi

val log =
  KotlinLogging.logger("DEMO").also {
    initLogging(it)
  }


@OptIn(ExperimentalNativeApi::class)
fun main(args:Array<String>){
  println("Hello from the demo")

  log.trace { "log trace"}
  log.debug { "log debug"}
  log.info { "log info"}
  log.warn { "log warn"}
  val err = Exception("Demo Exception")
  log.error(err){
    "example error message: ${err.getStackTrace().joinToString("\n")}"
  }
}