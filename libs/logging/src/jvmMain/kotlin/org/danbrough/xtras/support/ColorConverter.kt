package org.danbrough.xtras.support

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

  /*
  internal val Level.color: String
  get() = when (this) {
    Level.ERROR -> ANSIConstants.BOLD + ANSIConstants.RED_FG
    Level.WARN -> ANSIConstants.YELLOW_FG
    Level.INFO -> ANSIConstants.GREEN_FG
    Level.DEBUG -> ANSIConstants.CYAN_FG
    Level.TRACE -> ANSIConstants.MAGENTA_FG
    else -> ANSIConstants.DEFAULT_FG
  }
   */
  /*
      Level.TRACE -> 35
      Level.DEBUG -> 36
      Level.INFO -> 32
      Level.WARN -> 33
      Level.ERROR -> 31
      Level.OFF -> 0
    }
   */
}

/*
8      @Override
29      protected String getForegroundColorCode(ILoggingEvent event) {
30          Level level = event.getLevel();
31          switch (level.toInt()) {
32          case Level.ERROR_INT:
33              return BOLD + RED_FG;
34          case Level.WARN_INT:
35              return RED_FG;
36          case Level.INFO_INT:
37              return BLUE_FG;
38          default:
39              return DEFAULT_FG;
40          }
41
42      }
 */