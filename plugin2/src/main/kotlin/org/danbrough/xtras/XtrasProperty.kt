package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("MemberVisibilityCanBePrivate")
class XtrasProperty<T>(
  private val project: Project,
  private val name: String,
  private val type: KClass<*>,
  private val defaultValue: () -> T
) {

  @Suppress("UNCHECKED_CAST")
  fun getValue(): T {
    println("XtrasProperty:$name:getValue() type:$type defaultValue: $defaultValue")
    if (project.hasProperty(name)) {
      val value = project.property(name)
      if (value?.javaClass == type.java) return value as T
    }
    return defaultValue()
  }

  fun toProvider(): Provider<T> = project.provider { getValue() }

  @Suppress("UNCHECKED_CAST")
  fun toProperty(): Property<T> =
    (project.objects.property(type) as Property<T>).convention(toProvider())

  @Suppress("NOTHING_TO_INLINE")
  inline operator fun getValue(
    thisRef: Any?,
    property: KProperty<*>
  ): T = getValue()

  companion object {
    inline fun <reified T> Project.xtrasProperty(key: String) =
      xtrasProperty<T>(key) { error("Property $key not found in $path") }

    inline fun <reified T> Project.xtrasProperty(key: String, defaultValue: T) =
      xtrasProperty<T>(key) { defaultValue }

    inline fun <reified T> Project.xtrasProperty(
      key: String,
      noinline defaultValue: () -> T
    ) = XtrasProperty(this, key, T::class, defaultValue)

    inline fun <reified T> Project.getXtrasProperty(key: String): T =
      getXtrasProperty(key, T::class) { error("Property $key not found in $path") }


    inline fun <reified T> Project.getXtrasProperty(key: String, defaultValue: T): T =
      getXtrasProperty(key, T::class) { defaultValue }

    inline fun <reified T> Project.getXtrasProperty(
      key: String,
      noinline defaultValue: () -> T
    ): T = getXtrasProperty(key, T::class, defaultValue)

    inline fun <reified T> Project.getXtrasProperty(
      key: String,
      type: KClass<*>,
      noinline defaultValue: () -> T
    ): T {
      println("getXtrasProperty:$key:getValue() type:$type defaultValue: $defaultValue")
      if (project.hasProperty(key)) {
        val value = project.property(key)
        if (value?.javaClass == type.java) return value as T
      }
      return defaultValue()
    }

  }
}

