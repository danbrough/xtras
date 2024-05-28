package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias ScriptCommand = ScriptCommandContext.(KonanTarget)->Unit

class ScriptCommandContext(val writer: PrintWriter) {

}