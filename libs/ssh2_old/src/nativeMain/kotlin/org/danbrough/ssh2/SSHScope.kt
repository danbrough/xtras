package org.danbrough.ssh2

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValues
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.memScoped
import org.danbrough.ssh2.cinterops.libssh2_exit
import org.danbrough.ssh2.cinterops.libssh2_init

@Suppress("MemberVisibilityCanBePrivate")
actual class SSHScope(val mScope: MemScope) : Scope, NativePlacement by mScope {

  init {
    libssh2_init(0).also {
      if (it != 0) error("libssh2_init() -> $it") else log.trace { "libssh2_init()" }
    }
  }

  inline fun <R> memScoped(block: MemScope.() -> R): R = mScope.block()

  val <T : CVariable> CValues<T>.ptr: CPointer<T>
    get() = this@ptr.getPointer(mScope)

  actual override fun close() {
    log.trace { "SSHScope::release()" }
    libssh2_exit()
  }
}