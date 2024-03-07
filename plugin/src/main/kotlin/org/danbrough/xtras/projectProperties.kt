package org.danbrough.xtras

import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.net.URI
import kotlin.reflect.typeOf

inline fun <reified T> ExtensionAware.projectProperty(
  name: String,
  noinline defaultValue: (() -> T)? = null
): T =
  if (extra.has(name)) {

    val value = extra[name].toString()
    when (T::class) {
      String::class -> value
      Int::class -> value.toInt()
      Float::class -> value.toFloat()
      Double::class -> value.toDouble()
      Long::class -> value.toLong()
      Boolean::class -> value.toBoolean()
      File::class -> File(value)
      URI::class -> URI.create(value)
      else -> throw Error("Invalid property type: ${T::class}")
    } as T
  } else defaultValue?.invoke() ?: if (typeOf<T>().isMarkedNullable) null as T else
    error("Property $name not found and no default specified")





