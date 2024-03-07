package org.danbrough.xtras.tasks

import org.danbrough.xtras.LibraryExtension

internal fun LibraryExtension.registerSourceTasks() {
  when (sourceConfig) {
    is GitSource -> registerGitTasks()
  }
}


