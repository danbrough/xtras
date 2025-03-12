package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.provider.Property
import kotlin.reflect.KClass


inline fun <reified T : Any?> Project.xtrasProperty(
  key: String,
  noinline defaultValue: () -> T
): Property<T> = objects.property(T::class.java).apply {
  convention(provider {
    getXtrasPropertyValue<T>(key, defaultValue)
  })
}

inline fun <reified T : Any?> Project.xtrasProperty(key: String, defaultValue: T) =
  xtrasProperty(key) { defaultValue }


inline fun <reified T : Any?> Project.getXtrasPropertyValue(
  key: String,
  noinline defaultValue: () -> T
) = getXtrasPropertyValue(key, T::class, defaultValue)

inline fun <reified T : Any?> Project.getXtrasPropertyValue(
  key: String,
  type: KClass<*>,
  noinline defaultValue: () -> T
): T {
  //println("getXtrasProperty:$key:getValue() type:$type defaultValue: $defaultValue")


  if (!project.hasProperty(key)) return defaultValue()
  val value = project.property(key)!!

  if (value.javaClass == type.java) {
    return value as T
  }

  val stringValue = value.toString().trim()

  this.logger.warn("stringValue [$stringValue]")

  return when (T::class){
    Int::class -> stringValue.toInt() as T
    Long::class -> stringValue.toLong() as T
    Double::class -> stringValue.toDouble() as T
    Boolean::class -> stringValue.toBoolean() as T
    else -> defaultValue()
  }
}

/*
inline fun <reified T> Project.getXtrasPropertyValue(key: String): T =
  getXtrasPropertyValue(key, T::class) { error("Property $key not found in $path") }

inline fun <reified T> Project.getXtrasPropertyValue(key: String, defaultValue: T): T =
  getXtrasPropertyValue(key, T::class) { defaultValue }*/
