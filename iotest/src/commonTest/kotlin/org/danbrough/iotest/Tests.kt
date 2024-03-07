package org.danbrough.iotest

import kotlinx.io.buffered
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteString
import kotlin.test.Test


class Tests {

  @Test
  fun test() {
    log.trace { "test()" }
    log.debug { "test()" }
    log.info { "test()" }
    log.warn { "test()" }
    log.error { "test()" }
  }


  @Test
  fun testFiles() {
    log.info { "testFiles()" }


    val testFile = Path("/tmp/test.txt")
    val exists = SystemFileSystem.exists(testFile)
    log.debug { "testFile $testFile exists: $exists" }

    if (exists) {
      SystemFileSystem.source(testFile).buffered().readByteString().also {
        log.debug { "read: $it size: ${it.size}" }
        log.trace { it.decodeToString() }
      }
    }
  }
}