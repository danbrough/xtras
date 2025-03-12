package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.net.URI
import kotlin.reflect.typeOf

inline fun <reified T> Project.projectProperty(
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


inline fun <reified T : Any?> Project.xtrasProperty(
  name: String,
  noinline notFound: (() -> T)? = null
): T {
  val value = findProperty(name) ?: return notFound?.invoke() ?: null as T
  return when (T::class) {
    String::class -> value.toString()
    Int::class -> value.toString().toInt()
    Float::class -> value.toString().toFloat()
    Double::class -> value.toString().toDouble()
    Long::class -> value.toString().toLong()
    Boolean::class -> value.toString().let { it == "true" || it == "1" }
    File::class -> File(value.toString())
    URI::class -> URI.create(value.toString())
    else -> value
  } as T
}
