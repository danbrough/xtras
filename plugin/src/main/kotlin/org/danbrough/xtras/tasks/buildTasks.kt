package org.danbrough.xtras.tasks

import org.danbrough.xtras.XtrasDSL
import org.danbrough.xtras.XtrasLibrary
import org.danbrough.xtras.taskNameBuild
import org.danbrough.xtras.taskNamePackage
import org.danbrough.xtras.taskNamePackageExtract
import org.danbrough.xtras.taskNameSourceExtract
import org.danbrough.xtras.xInfo
import org.danbrough.xtras.xtrasName
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

@XtrasDSL
fun XtrasLibrary.buildScript(config: ScriptTask.() -> Unit) {
  project.afterEvaluate {

    xInfo(
      "${this@buildScript.name}:buildScript(): targets: ${
        buildTargets.get().joinToString()
      }"
    )

    buildTargets.get().forEach { target ->
      val taskNameBuild = taskNameBuild(target)
      val taskNamePackage = taskNamePackage(target)
      val sourceDir = sourcesDirMap(target)
      val toBeBuilt = buildEnabled.get().invoke(target)
      val packageFile = packageFileMap.invoke(target)
      val installDir = installDirMap(target)
      val libDir = libDirMap(target)

      tasks.register<ScriptTask>(taskNameBuild) {
        dependsOn(taskNameSourceExtract(target))


        this@register.target.set(target)
        outputDirectory.set(installDir)
        workingDir = sourceDir


        onlyIf { toBeBuilt }

        description = "Builds ${this@buildScript.name} for ${target.xtrasName}"
        config()
      }

      tasks.register<Exec>(taskNamePackage) {
        dependsOn(taskNameBuild)
        outputs.file(packageFile)
        workingDir = installDir

        doFirst {
          xInfo("packaging $name installDir: $workingDir")
        }

        commandLine("tar", "cvpfz", packageFile, "--exclude=**share", "--exclude=**pkgconfig", "./")
        onlyIf { toBeBuilt }

        doLast {
          sourceDir.takeIf { it.exists() }?.deleteRecursively()?.also {
            xInfo("deleted $sourceDir success:$it ")
          }
          installDir.takeIf { it.exists() }?.deleteRecursively()?.also {
            xInfo("deleted $installDir success:$it ")
          }
        }
      }

      tasks.register<Exec>(taskNamePackageExtract(target)) {
        if (!packageFile.exists())
          dependsOn(taskNamePackage)

        doFirst {
          if (libDir.exists()) libDir.deleteRecursively()
          libDir.mkdirs()
          workingDir(libDir)
        }


        outputs.dir(libDir)
        commandLine("tar", "xvpfz", packageFile)
      }
    }
  }
}

