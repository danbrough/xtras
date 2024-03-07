package org.danbrough.iotest

import io.github.oshai.kotlinlogging.DelegatingKLogger
import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level


internal actual fun init(log: KLogger) {
  log.info { "init()" }
  KotlinLoggingConfiguration.logLevel = Level.TRACE

  KotlinLoggingConfiguration.formatter = object : Formatter {
    val formatter = KotlinLoggingConfiguration.formatter
    override fun formatMessage(loggingEvent: KLoggingEvent) =
      formatter.formatMessage(loggingEvent).colored(loggingEvent.level)
  }


  if (log is DelegatingKLogger<*>) {
    log.underlyingLogger?.also {
      log.warn {
        "underlying logger: $it type: ${it::class.qualifiedName}"
      }
    }
  }

}