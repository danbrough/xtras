@file:Suppress("NOTHING_TO_INLINE")

package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import javax.inject.Inject


private val LogLevel.color: Int
  get() = when (this) {
    LogLevel.DEBUG -> 35
    LogLevel.INFO -> 36
    LogLevel.LIFECYCLE -> 36
    LogLevel.WARN -> 32
    LogLevel.QUIET -> 33
    LogLevel.ERROR -> 31
  }

internal fun String.colored(level: LogLevel) =
  "\u001b[0;${level.color}m$this\u001b[0m"

abstract class Logger {
  abstract val tag: String

  abstract fun log(msg: String, level: LogLevel, err: Throwable?)

  inline fun trace(msg: String, err: Throwable? = null) =
    log(msg, LogLevel.DEBUG, err)

  inline fun debug(msg: String, err: Throwable? = null) =
    log(msg, LogLevel.INFO, err)

  inline fun info(msg: String, err: Throwable? = null) =
    log(msg, LogLevel.WARN, err)

  inline fun warn(msg: String, err: Throwable? = null) =
    log(msg, LogLevel.QUIET, err)

  inline fun error(msg: String, err: Throwable? = null) =
    log(msg, LogLevel.ERROR, err)
}

@Suppress("MemberVisibilityCanBePrivate")
class XtrasLoggerImpl @Inject constructor(
  val project: Project?,
  override val tag: String,
  val logToStdout: Boolean,
  val logToGradle: Boolean
) : Logger() {


  //private val output: StyledTextOutput = project.gradle.serviceOf<StyledTextOutputFactory>().create("XtrasLogOutput")

  override fun log(msg: String, level: LogLevel, err: Throwable?) {
    if (logToStdout) {
      /*      val logName = when (level) {
              LogLevel.DEBUG -> "TRACE"
              LogLevel.INFO -> "DEBUG"
              LogLevel.LIFECYCLE -> "DEBUG"
              LogLevel.WARN -> " INFO"
              LogLevel.QUIET -> " WARN"
              LogLevel.ERROR -> "ERROR"
            }*/
      println(
        "${if (tag.length == 4) " " else ""}${tag.colored(level)}: ${msg.colored(level)} ${
          err?.message?.colored(
            level
          ) ?: ""
        }"
      )
    }

    if (logToGradle) project!!.logger.log(level, msg)
  }
}


private val loggers = mutableMapOf<Project, Logger>()

/*val logger: Logger by lazy {
  XtrasLoggerImpl(
    project,
    project.xtrasPropertyValue("$XTRAS_EXTN_NAME.log.tag") { "XTRAS" },
    logToStdout = project.xtrasPropertyValue("$XTRAS_EXTN_NAME.log.stdout") { true },
    logToGradle = project.xtrasPropertyValue("$XTRAS_EXTN_NAME.log.gradle") { false }
  )
}*/
val Project.xtrasLogger: Logger
  get() = loggers.getOrPut(this) {
    XtrasLoggerImpl(
      this@xtrasLogger,
      xtrasPropertyValue("$XTRAS_EXTN_NAME.log.tag") { "XTRAS" },
      logToStdout = xtrasPropertyValue("$XTRAS_EXTN_NAME.log.stdout") { true },
      logToGradle = xtrasPropertyValue("$XTRAS_EXTN_NAME.log.gradle") { false }
    )
  }


fun Project.xTrace(msg: String, err: Throwable? = null) =
  xtrasLogger.log(msg, LogLevel.DEBUG, err)

inline fun Project.xDebug(msg: String, err: Throwable? = null) =
  xtrasLogger.log(msg, LogLevel.INFO, err)

inline fun Project.xInfo(msg: String, err: Throwable? = null) =
  xtrasLogger.log(msg, LogLevel.WARN, err)

inline fun Project.xWarn(msg: String, err: Throwable? = null) =
  xtrasLogger.log(msg, LogLevel.QUIET, err)

inline fun Project.xError(msg: String, err: Throwable? = null) =
  xtrasLogger.log(msg, LogLevel.ERROR, err)

