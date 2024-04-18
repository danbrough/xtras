package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskConfig
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.logDebug
import org.danbrough.xtras.logTrace
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget


@XtrasDSL
fun XtrasLibrary.sourceTask(
  name: SourceTaskName,
  dependsOn: SourceTaskName?,
  block: Exec.(KonanTarget) -> Unit
): TaskConfig = { target ->


  project.tasks.register<Exec>(name.taskName(this, target)) {
    group = XTRAS_TASK_GROUP
    enabled = buildEnabled

    dependsOn?.also {
      dependsOn(it.taskName(this@sourceTask, target))
    }

    workingDir(sourceDir(target))
    doFirst {

      environment(loadEnvironment(environment, target))
      project.logDebug("$name: running command: ${commandLine.joinToString(" ")}")
      project.logTrace("$name: environment: $environment")
    }
    block(target)
  }
}
//  val taskName = ""//xtrasTaskName(TASK_GROUP_SOURCE, name.name.lowercase(), this, target)
//  project.tasks.register<Exec>(taskName) {
//    //TODO dependsOn(*dependencies.map { it.taskNamePackageExtract(target) }.toTypedArray())
//    group = XTRAS_TASK_GROUP
//    //TODO environment(xtras.buildEnvironment.getEnvironment(target))
////    onlyIf {
////      forceBuild() || !packageFile(target).exists()
////    }
////    if (dependsOn != null)
////      dependsOn(
////        xtrasTaskName(
////          TASK_GROUP_SOURCE,
////          dependsOn.name.lowercase(),
////          this@sourceTask,
////          target
////        )
////      )
//
//    //workingDir(sourceDir(target))
//    doFirst {
//      project.logDebug("$name: running command: ${commandLine.joinToString(" ")}")
//    }
//    block(target)


@XtrasDSL
fun XtrasLibrary.prepareSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskPrepareSource = sourceTask(SourceTaskName.PREPARE, dependsOn) {
    block(it)
  }
}


@XtrasDSL
fun XtrasLibrary.configureSource(
  dependsOn: SourceTaskName? = SourceTaskName.EXTRACT,
  block: Exec.(KonanTarget) -> Unit
) {
  taskConfigureSource = sourceTask(SourceTaskName.CONFIGURE, dependsOn) {
    block(it)
  }
}


@XtrasDSL
fun XtrasLibrary.compileSource(
  dependsOn: SourceTaskName? = SourceTaskName.CONFIGURE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskCompileSource = sourceTask(SourceTaskName.COMPILE, dependsOn) { target ->
    val taskFile = workingDir.resolve(".xtras_compiled")
    outputs.file(taskFile)
    doLast {
      taskFile.createNewFile()
    }
    block(target)
  }
}


@XtrasDSL
fun XtrasLibrary.installSource(
  dependsOn: SourceTaskName? = SourceTaskName.COMPILE,
  block: Exec.(KonanTarget) -> Unit
) {
  taskInstallSource = sourceTask(SourceTaskName.INSTALL, dependsOn) { target ->
    outputs.dir(buildDir(target))
    block(target)
  }
}
