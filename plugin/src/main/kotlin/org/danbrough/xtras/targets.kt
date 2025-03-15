package org.danbrough.xtras

import org.jetbrains.kotlin.konan.target.KonanTarget

val KonanTarget.xtrasName: String
  get() = name.split("_").let {
    it[0] + it.subList(1, it.size)
      .joinToString("") { part -> part.replaceFirstChar { firstChar -> firstChar.uppercase() } }
  }