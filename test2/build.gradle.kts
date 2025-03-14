import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmRun

plugins {
  application
  kotlin("jvm")
}


application {
  mainClass = "test.FooKt"
}


dependencies {
  //implementation(kotlin("reflect"))
  //implementation(kotlin("stdlib"))
  implementation(libs.xtras.plugin)


}

afterEvaluate {
  @OptIn(InternalKotlinGradlePluginApi::class)
  tasks.withType<KotlinJvmRun> {
    args = listOf("A", "B", "c")
  }
}
