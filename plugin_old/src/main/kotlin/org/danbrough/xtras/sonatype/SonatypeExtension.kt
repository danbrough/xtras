package org.danbrough.xtras.sonatype

import org.gradle.api.provider.Property

abstract class SonatypeExtension {
  companion object {
    const val REPO_NAME = "Sonatype"
    const val EXTENSION_NAME = "sonatype"
    const val REPO_ID = "sonatype.repoID"
    const val PROFILE_ID = "sonatype.profileID"
    const val USERNAME = "sonatype.username"
    const val PASSWORD = "sonatype.password"
    const val DESCRIPTION = "sonatype.description"
  }

  abstract val urlBase: Property<String>
  abstract val repoID: Property<String>
  abstract val profileID: Property<String>
  abstract val username: Property<String>
  abstract val password: Property<String>
  abstract val description: Property<String>

}