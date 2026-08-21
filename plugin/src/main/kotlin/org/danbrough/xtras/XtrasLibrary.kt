package org.danbrough.xtras

import org.danbrough.xtras.tasks.CInteropsConfig
import org.danbrough.xtras.tasks.registerPackageResolveTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.net.URL
import kotlin.reflect.KClass

class ScriptEnvironment(env: MutableMap<String, Any> = mutableMapOf()) :
  MutableMap<String, Any> by env {
  fun append(name: String, value: Any) {
    set(name, get(name)?.let { "$it " } ?: value)
  }
}

@XtrasDSL
@Suppress("MemberVisibilityCanBePrivate")
open class XtrasLibrary(val xtras: Xtras, val project: Project, val name: String) {

  interface SourceConfig

  interface GitSourceConfig : SourceConfig {
    val url: URL
    val commit: String
  }

  val version: Property<String> = project.xtrasProperty<String>("$name.version")

  val group: Property<String> =
    project.xtrasProperty<String>("$name.group") { project.group.toString() }

  var sourceConfig: SourceConfig? = null

  val cacheDir: File
    get() = project.xtrasCacheDir.resolve(name)

  var subPathMap: File.(KonanTarget) -> File = { target ->
    resolve("${this@XtrasLibrary.name}_${target.xtrasName}_${version.get()}")
  }

  var installDirMap: (KonanTarget) -> File = {
    project.xtrasBuildDir.subPathMap(it)
  }

  val interopsFile: RegularFileProperty = project.objects.fileProperty().convention {
    project.xtrasBuildDir.resolve("interops")
      .resolve("${this@XtrasLibrary.name}_${version.get()}.def")
  }

  val buildEnabled: Provider<((KonanTarget) -> Boolean)> =
    project.objects.property<(KonanTarget) -> Boolean>().convention(project.provider {
      { !packageFileMap.invoke(it).exists() }
    })


  var sourcesDirMap: (KonanTarget) -> File = {
    project.xtrasSrcDir.subPathMap(it)
  }

  var libDirMap: (KonanTarget) -> File = {
    project.xtrasLibDir.subPathMap(it)
  }

  var packageFileMap: (KonanTarget) -> File = {
    project.xtrasPackagesDir.resolveAll(
      name,
      group.get(),
      version.get(),
      it.xtrasName,
      "xtras_${name}_${version.get()}_${it.xtrasName}.tgz"
    )
  }

  var buildTargets: ListProperty<KonanTarget> =
    project.objects.listProperty<KonanTarget>().convention(project.provider {
      val kotlin = project.kotlinExtension
      if (kotlin is KotlinMultiplatformExtension) {
        kotlin.targets.filterIsInstance<KotlinNativeTarget>().map { it.konanTarget }
      } else emptyList()
    })


  @XtrasDSL
  var binaryPackageArtifactID: (KonanTarget) -> String = {
    "binaries-${name}-${it.kotlinTargetName.lowercase()}"
  }

  @Optional
  internal val cinterops: Property<CInteropsConfig> = project.objects.property()


  val publishBinaries: Property<Boolean> = project.xtrasProperty("$name.publishBinaries", true)

  override fun toString(): String = "$name:${version.get()}"

}


inline fun <reified T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, noinline block: T.() -> Unit = {}
): T = xtrasRegisterLibrary(name, block, T::class)

fun <T : XtrasLibrary> Project.xtrasRegisterLibrary(
  name: String, block: T.() -> Unit = {}, type: KClass<T>
): T {
  rootProject.extensions.findByName(XTRAS_EXTN_NAME) ?: pluginManager.apply(XtrasPlugin::class.java)

  val xtras = rootProject.extensions.findByType<Xtras>()
    ?: error("Expecting Xtras extension to have been created")

  return extensions.create(name, type, xtras, this, name).apply(block).also {
    it.xtrasConfigureLibrary(xtras)
  }

}


internal fun XtrasLibrary.xtrasConfigureLibrary(xtras: Xtras) {

  xInfo("xtrasConfigureLibrary(): $name xtras:$xtras publishBinaries: ${publishBinaries.get()}")
  if (publishBinaries.get()) {
    publishBinaries()
  }

}


private fun XtrasLibrary.registerBinaryPublication(target: KonanTarget) {
  val publishing = project.extensions.findByType<PublishingExtension>() ?: return

  val resolvePackageTaskName = TaskNames.create(TaskNames.GROUP_PACKAGE, "resolve", name, target)
  val artifactTask = project.tasks.getByName(resolvePackageTaskName)
  val publicationName = "${name}Binaries${target.kotlinTargetName.capitalized()}"

  publishing.publications.create<MavenPublication>(publicationName) {
    artifactId = binaryPackageArtifactID(target)
    version = this@registerBinaryPublication.version.get()
    groupId = this@registerBinaryPublication.group.get()

    val file = artifactTask.outputs.files.first()
    //project.logError("registerBinaryPublication for file: ${file.absolutePath}")
    artifact(file).builtBy(artifactTask)
    pom {
      packaging = "tgz"
    }
  }
}


fun XtrasLibrary.resolveBinariesFromMaven(target: KonanTarget): File? {
  val mavenID = "${group.get()}:${binaryPackageArtifactID(target)}:${version.get()}"
  xDebug("XtrasLibrary[$name]::resolveBinariesFromMaven():$target $mavenID")

  val binariesConfiguration =
    project.configurations.create("configuration${this@resolveBinariesFromMaven.name.capitalized()}Binaries${target.kotlinTargetName.capitalized()}") {
/*      isVisible = false
      isTransitive = false
      isCanBeConsumed = true
      isCanBeResolved = true*/
    }

  project.dependencies {
    binariesConfiguration(mavenID)
  }


  runCatching {
    return binariesConfiguration.resolve().first().also {
      xDebug("XtrasLibrary[$name]::resolveBinariesFromMaven(): $target found ${it.absolutePath}")
    }
  }.exceptionOrNull()?.let {
    xError("XtrasLibrary[$name]::resolveBinariesFromMaven():$target Failed for $mavenID: ${it.message}")
  }
  return null
}

internal fun XtrasLibrary.publishBinaries() {
  xInfo("XtrasLibrary::$name.publishBinaries()")
  project.afterEvaluate {
    withPublishing {
      buildTargets.get().forEach { target ->
        registerPackageResolveTask(target)
        registerBinaryPublication(target)
      }
    }
  }
}

