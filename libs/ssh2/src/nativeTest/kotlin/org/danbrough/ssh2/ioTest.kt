@file:OptIn(ExperimentalStdlibApi::class)

package org.danbrough.ssh2

/*import kotlinx.io.buffered
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteString*/


fun mainIoTest(args: Array<String>) {
  log.info { "mainIoTest()" }
  /*
    val testFile = Path("/tmp/test.txt")

    log.debug { "tmpDir: $SystemTemporaryDirectory" }



    log.debug { "testFile: $testFile exists: ${SystemFileSystem.exists(testFile)}" }
    if (SystemFileSystem.exists(testFile)) {
      SystemFileSystem.source(testFile).buffered().use {
        log.info { "content: ${it.readByteString().decodeToString()}" }
      }
    }
  */


}