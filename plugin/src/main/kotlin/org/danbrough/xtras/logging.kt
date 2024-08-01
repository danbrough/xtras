@file:Suppress("NOTHING_TO_INLINE")

package org.danbrough.xtras


import org.gradle.api.Project
import org.gradle.api.logging.LogLevel


private const val logToStdoutProperty = "xtras.log.stdout"
private const val logToGradleProperty = "xtras.log.gradle"


internal val LogLevel.color: Int
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


private var logToStdout: Boolean? = null
private inline fun logToStdout(project: Project): Boolean =
  logToStdout ?: project.projectProperty(logToStdoutProperty) { false }
    .also {
      logToStdout = it
    }


private var logToGradle: Boolean? = null
private inline fun logToGradle(project: Project): Boolean =
  logToGradle ?: project.projectProperty(
    logToGradleProperty
  ) { true }
    .also {
      logToGradle = it
    }


fun Project.log(msg: String, level: LogLevel = LogLevel.INFO, err: Throwable? = null) {

  if (logToStdout(this)) {
    val logName = when (level) {
      LogLevel.DEBUG -> "TRACE"
      LogLevel.INFO -> "DEBUG"
      LogLevel.LIFECYCLE -> error("LogLevel.LIFECYCLE not used")
      LogLevel.WARN -> "INFO"
      LogLevel.QUIET -> "WARN"
      LogLevel.ERROR -> "ERROR"
    }
    println(
      "${if (logName.length == 4) " " else ""}${logName.colored(level)}: ${project.name.colored(level)}: ${msg.colored(level)} ${
        err?.message?.colored(
          level
        ) ?: ""
      }"
    )
  }

  if (logToGradle(this)) logger.log(level, msg)
}


inline fun Project.logTrace(msg: String, err: Throwable? = null) = log(msg, LogLevel.DEBUG, err)
inline fun Project.logDebug(msg: String, err: Throwable? = null) = log(msg, LogLevel.INFO, err)
inline fun Project.logInfo(msg: String, err: Throwable? = null) = log(msg, LogLevel.WARN, err)
inline fun Project.logWarn(msg: String, err: Throwable? = null) = log(msg, LogLevel.QUIET, err)
inline fun Project.logError(msg: String, err: Throwable? = null) = log(msg, LogLevel.ERROR, err)
