package org.danbrough.xtras.tasks

import org.danbrough.xtras.TaskConfig
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
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
    dependsOn?.also {
      dependsOn(it.taskName(this@sourceTask, target))
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



