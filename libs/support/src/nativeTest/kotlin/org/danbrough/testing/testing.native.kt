package org.danbrough.testing
/*

import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

fun initTesting() {
  KotlinLoggingConfiguration.logLevel = Level.TRACE
  KotlinLoggingConfiguration.formatter = object : Formatter {
    val formatter = KotlinLoggingConfiguration.formatter
    override fun formatMessage(loggingEvent: KLoggingEvent) =
      formatter.formatMessage(loggingEvent).colored(loggingEvent.level)
  }
}


object ANSIConstants {
  const val ESC_START = "\u001b["
  const val ESC_END = "m"
  const val BOLD = "1;"
  const val RESET = "0;"
  const val BLACK_FG = "30"
  const val RED_FG = "31"
  const val GREEN_FG = "32"
  const val YELLOW_FG = "33"
  const val BLUE_FG = "34"
  const val MAGENTA_FG = "35"
  const val CYAN_FG = "36"
  const val WHITE_FG = "37"
  const val DEFAULT_FG = "39"
}


internal val Level.color: String
  get() = when (this) {
    Level.ERROR -> ANSIConstants.BOLD + ANSIConstants.RED_FG
    Level.WARN -> ANSIConstants.YELLOW_FG
    Level.INFO -> ANSIConstants.GREEN_FG
    Level.DEBUG -> ANSIConstants.CYAN_FG
    Level.TRACE -> ANSIConstants.MAGENTA_FG
    else -> ANSIConstants.DEFAULT_FG
  }

internal fun String.colored(level: Level) =
  "${ANSIConstants.ESC_START}${level.color}m$this${ANSIConstants.ESC_START}m"*/
