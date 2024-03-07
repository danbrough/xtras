package org.danbrough.xtras.env

import org.danbrough.xtras.XtraDSL

data class Binaries(

  @XtraDSL
  var git: String = "git",

  @XtraDSL
  var wget: String = "wget",

  @XtraDSL
  var tar: String = "tar",

  @XtraDSL
  var autoreconf: String = "autoreconf",

  @XtraDSL
  var make: String = "make",

  @XtraDSL
  var cmake: String = "cmake",

  @XtraDSL
  var go: String = "go",

  @XtraDSL
  var bash: String = "bash",

  @XtraDSL
  var cygpath: String = "cygpath",
)