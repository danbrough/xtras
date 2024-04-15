package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.tasks.CInteropsConfig.Companion.defaultCInteropsTargetWriter
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias CInteropsTargetWriter = XtrasLibrary.(KonanTarget, PrintWriter) -> Unit

data class CInteropsConfig(

  /**
   * The list of headers to form the stop of the generated def file
   */

  var declaration: String? = null,

  /**
   * Specifies the path to a hard-coded def file to use.
   * All other settings will be ignored.
   */
  var defFile: File? = null,


  /**
   * After the [declaration] this will generate target specific headers for the generated def file.
   * Default implementation at [defaultCInteropsTargetWriter].
   */
  var targetWriter: CInteropsTargetWriter = defaultCInteropsTargetWriter,

  /**
   * Code to append to the bottom of the def file
   *
   */
  var code: String? = null,

  /**
   * Path to hard coded content for the def file to be appended at the bottom.
   *
   */
  var codeFile: File? = null,

  ) {
  companion object {
    val defaultCInteropsTargetWriter: CInteropsTargetWriter = { target, writer ->
      val libDir = libsDir(target).absolutePath
      writer.println(
        """
         |compilerOpts.${target.name} =  -I${
          "$libDir${org.jetbrains.kotlin.konan.file.File.separatorChar}include".replace(
            '\\',
            '/'
          )
        }
         |linkerOpts.${target.name} = -L${
          "$libDir${org.jetbrains.kotlin.konan.file.File.separatorChar}lib".replace(
            '\\',
            '/'
          )
        }
         |libraryPaths.${target.name} =  ${
          "$libDir${org.jetbrains.kotlin.konan.file.File.separatorChar}lib".replace(
            '\\',
            '/'
          )
        }
         |""".trimMargin()
      )
    }
  }
}