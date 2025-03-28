package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

class XtrasAndroid(project: Project) {

  val sdkVersion: Property<Int> = project.xtrasProperty("$XTRAS_EXTN_NAME.android.sdk.version", 35)
  val ndkVersion: Property<Int> = project.xtrasProperty("$XTRAS_EXTN_NAME.android.ndk.version", 21)

  val ndkDir: Property<File> = project.xtrasProperty("$XTRAS_EXTN_NAME.dir.ndk")
}