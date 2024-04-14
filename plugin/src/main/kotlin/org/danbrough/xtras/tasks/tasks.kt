package org.danbrough.xtras.tasks


const val TASK_GROUP_SOURCE = "source"
const val TASK_GROUP_PACKAGE = "package"

enum class SourceTaskName {
  DOWNLOAD, EXTRACT, PREPARE, CONFIGURE, COMPILE, INSTALL
}

enum class PackageTaskName {
  CREATE, EXTRACT, DOWNLOAD, PROVIDE
}