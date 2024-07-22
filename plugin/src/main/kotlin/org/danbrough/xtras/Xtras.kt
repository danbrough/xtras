@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package org.danbrough.xtras

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@DslMarker
annotation class XtrasDSL

const val XTRAS_GROUP = "org.danbrough.xtras"
const val XTRAS_REPO_NAME = "Xtras"
const val XTRAS_LOCAL_REPO_NAME = "Local"
const val XTRAS_SONATYPE_REPO_NAME = "Sonatype"
const val XTRAS_TASK_GROUP = "xtras"
const val ENV_BUILD_DIR = "BUILD_DIR"
const val XTRAS_EXTENSION_NAME = "xtras"

abstract class Xtras(val project: Project) {

  companion object Constants {
    const val SONATYPE_REPO_NAME = "Sonatype"
    const val RSYNC_REPO_NAME = "RSync"

    object Properties {
      const val PROJECT_NAME = "project.name"
      const val PROJECT_DESCRIPTION = "project.description"
      const val PROJECT_GROUP = "project.group"
      const val PROJECT_VERSION = "project.version"

      const val PUBLISH_SIGN = "publish.sign"
      const val PUBLISH_DOCS = "publish.docs"
      const val PUBLISH_LOCAL = "publish.local"
      const val PUBLISH_XTRAS = "publish.xtras"

      /**
       * Whether to enable publishing to sonatype
       */
      const val PUBLISH_SONATYPE = "publish.sonatype"
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
    }
  }


  @XtrasDSL
  var javaVersion = JavaVersion.VERSION_11

  @XtrasDSL
  var jvmTarget = JvmTarget.JVM_11

  @XtrasDSL
  var kotlinLanguageVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  var kotlinApiVersion = KotlinVersion.KOTLIN_2_0

  @XtrasDSL
  abstract val nativeTargets: ListProperty<KonanTarget>

  private var environment: XtrasEnvironmentConfig = INITIAL_ENVIRONMENT

  @XtrasDSL
  abstract val libraries: ListProperty<XtrasLibrary>

  @XtrasDSL
  abstract val ldLibraryPath: Property<String>

  abstract val repoIDFileName: Property<String>
  abstract val repoIDFile: RegularFileProperty

  fun loadEnvironment(env: XtrasEnvironment, target: KonanTarget?): XtrasEnvironment {
    environment(env, target)
    return env
  }

  @XtrasDSL
  fun environment(block: XtrasEnvironmentConfig) {
    environment.also { oldEnvironment ->
      environment = { target ->
        oldEnvironment(target)
        block(target)
      }
    }
  }

  data class AndroidConfig(
    var ndkDir: File,
    var compileSDKVersion: Int = 34,
    var minSDKVersion: Int = 21,
    var ndkApiVersion: Int = minSDKVersion
  )

  val androidConfig = AndroidConfig(project.xtrasNdkDir)

  @XtrasDSL
  fun androidConfig(block: AndroidConfig.() -> Unit) {
    androidConfig.block()
  }

  @XtrasDSL
  var sh: File = project.projectProperty("xtras.sh") {
    if (HostManager.hostIsMingw)
      project.xtrasMsysDir.resolveAll("usr", "bin", "sh")
    else File("/bin/sh")
  }

}





