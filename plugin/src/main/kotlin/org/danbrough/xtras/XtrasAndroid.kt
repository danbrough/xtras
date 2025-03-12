package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property

class XtrasAndroid(project: Project) {

  val sdkVersion: Property<Int> = project.xtrasProperty("xtras.android.sdk.version",35)

}