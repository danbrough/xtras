package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.PrintWriter

typealias ScriptCommand = ScriptCommandContext.(KonanTarget)->Unit

data class ScriptCommandContext(val writer: PrintWriter) {
  
}