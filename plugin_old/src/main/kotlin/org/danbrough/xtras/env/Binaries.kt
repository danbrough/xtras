package org.danbrough.xtras.env

import org.danbrough.xtras.XtrasDSL

data class Binaries(

  @XtrasDSL
  var git: String = "git",

  @XtrasDSL
  var wget: String = "wget",

  @XtrasDSL
  var tar: String = "tar",

  @XtrasDSL
  var autoreconf: String = "autoreconf",

  @XtrasDSL
  var make: String = "make",

  @XtrasDSL
  var cmake: String = "cmake",

  @XtrasDSL
  var go: String = "go",

  @XtrasDSL
  var bash: String = "bash",

  @XtrasDSL
  var cygpath: String = "cygpath",
)