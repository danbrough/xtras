package org.danbrough.xtras

import org.danbrough.xtras.tasks.defaultCInteropsTargetWriter
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import kotlin.reflect.KProperty

@Suppress("unused")
class CInteropsConfig(var interopsPackage: String = "",var defFile: File) {
  var headers: String = ""
  var code: String? = null
  var codeFile: File? = null
  var cinteropsTargetWriter: CInteropsTargetWriter = defaultCInteropsTargetWriter

  override fun toString(): String = "CInterops[$interopsPackage,$defFile:header]"


}
