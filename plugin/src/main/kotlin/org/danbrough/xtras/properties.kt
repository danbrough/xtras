package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File
import java.net.URI
import kotlin.reflect.KClass


inline fun <reified T : Any?> Project.xtrasProperty(
  key: String,
  noinline defaultValue: () -> T = { error("$key not set") }
): Property<T> = objects.property(T::class.java).apply {
  convention(provider {
    xtrasPropertyValue<T>(key, T::class, defaultValue)
  })
}

inline fun <reified T : Any?> Project.xtrasProperty(key: String, defaultValue: T) =
  xtrasProperty(key) { defaultValue }

inline fun <reified T : Any?> ExtensionAware.xtrasPropertyValue(
  key: String,
  type: KClass<*> = T::class,
  noinline defaultValue: () -> T = { error("$key not specified") }
): T {
  //println("getXtrasProperty:$key:getValue() type:$type defaultValue: $defaultValue")
  val props = this.extraProperties

  if (!props.has(key)) return defaultValue()
  val value = props[key]!!

  // println("value for $key is $value")

  if (value.javaClass == type.java)
    return value as T

  val stringValue = value.toString().trim()

  //this.logger.warn("stringValue [$stringValue]")

  return when (T::class) {
    Int::class -> stringValue.toInt() as T
    Long::class -> stringValue.toLong() as T
    Double::class -> stringValue.toDouble() as T
    Boolean::class -> stringValue.toBoolean() as T
    URI::class -> URI.create(stringValue) as T
    File::class -> File(stringValue) as T
    else -> error("Unsupported type: ${T::class}")
  }
}

