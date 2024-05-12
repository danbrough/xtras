package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasLibrary
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

 fun XtrasLibrary.registerBuildTask(target: KonanTarget){
	 project.tasks.register<Exec>(SourceTaskName.BUILD.taskName(this,target)){
		 commandLine("echo","The date is `date`")
	 }
}