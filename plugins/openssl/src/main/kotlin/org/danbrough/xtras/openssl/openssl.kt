package org.danbrough.xtras.openssl

import org.danbrough.xtras.LibraryExtension
import org.danbrough.xtras.XTRAS_PACKAGE
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.tasks.gitSource
import org.danbrough.xtras.xtrasRegisterLibrary
import org.gradle.api.Project

const val OPENSSL_EXTN_NAME = "openssl"
const val PROPERTY_OPENSSL_VERSION = "openssl.version"
const val PROPERTY_OPENSSL_COMMIT = "openssl.commit"
const val PROPERTY_OPENSSL_URL = "openssl.url"


open class OpenSSLLibrary(name: String, version: String, project: Project) :
  LibraryExtension(name, version, project)


fun Project.openssl(
  version: String = projectProperty<String>(PROPERTY_OPENSSL_VERSION),
  url: String = projectProperty<String>(PROPERTY_OPENSSL_URL),
  commit: String = projectProperty<String>(PROPERTY_OPENSSL_COMMIT),
  block: OpenSSLLibrary.() -> Unit
): OpenSSLLibrary =
  xtrasRegisterLibrary<OpenSSLLibrary>(OPENSSL_EXTN_NAME,version) {
    gitSource(url, commit)
    block()
  }
