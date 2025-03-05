package org.danbrough.xtras

import org.gradle.api.Project
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class XtrasProperty<T : Any>(
  private val project: Project,
  private val name: String,
  private val type: KClass<T>,
  private val defaultValue: () -> T
) {

  @Suppress("UNCHECKED_CAST")
  fun getValue(): T {
    println("GETTING VALUE: name:$name: type:$type defaultValue: $defaultValue")
    if (project.hasProperty(name)){
      val value = project.property(name)
      if (value?.javaClass == type.java) return value as T
    }
    return defaultValue()
  }

  @Suppress("NOTHING_TO_INLINE")
  inline operator fun getValue(
    thisRef: Any?,
    property: KProperty<*>
  ): T = getValue()

  companion object {
    inline fun <reified T : Any> Project.xtrasProperty(name: String) =
      xtrasProperty<T>(name) { error("Property $name not found in $path") }

    inline fun <reified T : Any> Project.xtrasProperty(name: String, defaultValue: T) =
      xtrasProperty<T>(name) { defaultValue }

    inline fun <reified T : Any> Project.xtrasProperty(
      name: String,
      noinline defaultValue: () -> T
    ) = XtrasProperty(this, name, T::class, defaultValue)

  }
}

