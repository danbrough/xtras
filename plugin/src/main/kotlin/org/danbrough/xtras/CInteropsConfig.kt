package org.danbrough.xtras

import org.danbrough.xtras.tasks.defaultCInteropsTargetWriter
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias CInteropsTargetWriter = XtrasLibrary.(CInteropsConfig, KonanTarget, PrintWriter) -> Unit
typealias ExtraLibsDirectory = (KonanTarget) -> File

data class CInteropsConfig(

  /**
   * Specifies where the generated def file will be written.
   */
  var defFile: File,


  /**
   * An alternative value for the default cinterops package. "[project group].cinterops".
   */
  var interopsPackage: String,

  /**
   * If true then the [defFile] is used as is and is not generated.
   * default: false
   */
  var isStatic: Boolean = false,


  /**
   * The list of headers to form the stop of the generated def file
   */

  var declaration: String? = null,


  /**
   * After the [declaration] this will generate target specific headers for the generated def file.
   * Default implementation at [defaultCInteropsTargetWriter].
   */
  var targetWriter: CInteropsTargetWriter = defaultCInteropsTargetWriter,

  /**
   * Whether a target is included in the [targetWriter] output.
   */
  var targetWriterFilter: (KonanTarget) -> Boolean = { true },

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


  var extraLibsDirs: List<ExtraLibsDirectory> = emptyList(),

  )


