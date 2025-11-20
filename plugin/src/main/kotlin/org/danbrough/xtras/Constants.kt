package org.danbrough.xtras

import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.konan.target.KonanTarget

object Constants {
  const val XTRAS_GROUP = "org.danbrough.xtras"
  const val XTRAS_REPO_NAME = "Xtras"
  const val XTRAS_LOCAL_REPO_NAME = "Local"
  const val XTRAS_SONATYPE_REPO_NAME = "Sonatype"
  const val XTRAS_TASK_GROUP = "xtras"

  const val SONATYPE_REPO_NAME = "Sonatype"
  const val RSYNC_REPO_NAME = "RSync"

  object Properties {
    const val PROJECT_NAME = "project.name"
    const val PROJECT_DESCRIPTION = "project.description"
    const val PROJECT_GROUP = "project.group"
    const val PROJECT_VERSION = "project.version"

    const val PUBLISH_SIGN = "publish.sign"
    const val PUBLISH_DOCS = "publish.docs"

    /**
     * Whether to enable xtras publishing configuration
     */
    const val XTRAS_PUBLISHING = "xtras.publishing"
    const val SONATYPE_USERNAME = "sonatype.username"
    const val SONATYPE_PASSWORD = "sonatype.password"


    const val SONATYPE_PROFILE_ID = "sonatype.profileID"

    /**
     * Default: "https://s01.oss.sonatype.org"
     */
    const val SONATYPE_BASE_URL = "sonatype.baseURL"

    /**
     * Whether to publish to sonatype snapshots
     * Default: false
     */
    const val SONATYPE_SNAPSHOT = "sonatype.snapshot"


    /**
     * Specify the publishing repository staging id explicitly
     */
    const val SONATYPE_REPO_ID = "sonatype.repoID"

    /**
     * Whether to open a new repository explicitly:
     * Default: true
     */

    const val SONATYPE_OPEN_REPOSITORY = "sonatype.openRepository"

    /**
     * whether to automatically close any explicitly opened repositories
     * Default: false
     */

    const val SONATYPE_CLOSE_REPOSITORY = "sonatype.closeRepository"

    /**
     * Description for the explictly opened repository
     */
    const val SONATYPE_DESCRIPTION = "sonatype.description"

    //gpg in memory key for signing
    const val SIGNING_KEY = "signing.key"

    //gpg in memory password for signing
    const val SIGNING_PASSWORD = "signing.password"
  }

  object TaskNames {
    const val SONATYPE_OPEN_REPO = "sonatypeOpenRepository"
    const val SONATYPE_CLOSE_REPO = "sonatypeCloseRepository"
    const val XTRAS_PREPARE_JNI_LIBS = "xtrasPrepareJniLibs"

    fun copyAndroidLibsToJniFolderTaskName(binary: NativeBinary) =
      "xtrasCopyAndroidLibsToJniFolder${binary.name.capitalized()}${binary.target.konanTarget.kotlinTargetName.capitalized()}"

    fun copyXtrasLibsToJniFolderTaskName(lib: XtrasLibrary, target: KonanTarget) =
      "xtrasCopyXtrasLibsToJniFolder${lib.name.capitalized()}${target.kotlinTargetName.capitalized()}"
  }
}