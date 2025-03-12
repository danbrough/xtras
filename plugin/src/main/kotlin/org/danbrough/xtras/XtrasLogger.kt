@file:Suppress("NOTHING_TO_INLINE")

package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.getByType
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


@Suppress("MemberVisibilityCanBePrivate")
open class XtrasLogger @Inject constructor(val project: Project) {
  val tag: String = project.xtrasProperty("xtras.logger.tag") { "XTRAS" }.get()

  val logToStdout: Property<Boolean> = project.xtrasProperty("xtras.log.stdout", true)
  val logToGradle: Property<Boolean> = project.xtrasProperty("xtras.log.gradle", false)

  //private val output: StyledTextOutput = project.gradle.serviceOf<StyledTextOutputFactory>().create("XtrasLogOutput")


  fun log(msg: String, level: LogLevel = LogLevel.INFO, err: Throwable? = null) {

    if (logToStdout.get()) {
      val logName = when (level) {
        LogLevel.DEBUG -> "TRACE"
        LogLevel.INFO -> "DEBUG"
        LogLevel.LIFECYCLE -> "DEBUG"
        LogLevel.WARN -> "INFO"
        LogLevel.QUIET -> "WARN"
        LogLevel.ERROR -> "ERROR"
      }
      println(
        "${if (tag.length == 4) " " else ""}${tag.colored(level)}: ${
          project.name.colored(level)
        }: ${msg.colored(level)} ${
          err?.message?.colored(
            level
          ) ?: ""
        }"
      )
    }

    if (logToGradle.get()) project.logger.log(level, msg)
  }

  inline fun xtrasLog(msg: String, level: LogLevel, err: Throwable?) =
    log(msg, level, err)

  inline fun trace(msg: String, err: Throwable? = null) =
    xtrasLog(msg, LogLevel.DEBUG, err)

  inline fun debug(msg: String, err: Throwable? = null) =
    xtrasLog(msg, LogLevel.INFO, err)

  inline fun info(msg: String, err: Throwable? = null) =
    xtrasLog(msg, LogLevel.WARN, err)

  inline fun warn(msg: String, err: Throwable? = null) =
    xtrasLog(msg, LogLevel.QUIET, err)

  inline fun error(msg: String, err: Throwable? = null) =
    xtrasLog(msg, LogLevel.ERROR, err)

  companion object {
    val Project.xtrasLogger: XtrasLogger
      get() = extensions.getByType<Xtras>().logger


  }
}