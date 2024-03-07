package org.danbrough.iotest

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants.CYAN_FG
import ch.qos.logback.core.pattern.color.ANSIConstants.DEFAULT_FG
import ch.qos.logback.core.pattern.color.ANSIConstants.GREEN_FG
import ch.qos.logback.core.pattern.color.ANSIConstants.MAGENTA_FG
import ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG
import ch.qos.logback.core.pattern.color.ANSIConstants.YELLOW_FG
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

class ColorConverter : ForegroundCompositeConverterBase<ILoggingEvent>() {
  override fun getForegroundColorCode(event: ILoggingEvent): String =
    when (event.level.toInt()) {
      Level.ERROR_INT -> RED_FG
      Level.WARN_INT -> YELLOW_FG
      Level.INFO_INT -> GREEN_FG
      Level.DEBUG_INT -> CYAN_FG
      Level.TRACE_INT -> MAGENTA_FG
      else -> DEFAULT_FG
    }
}
