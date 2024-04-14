package org.danbrough.xtras.sonatype


import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.log
import org.gradle.api.Project
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.Base64

internal fun Project.sonatypeCloseRepository(
  stagingProfileId: String?,
  repoId: String?,
  description: String?,
  username: String?,
  password: String?,
  urlBase: String
) {
  log("sonatypeOpenRepository()")
  val url = "$urlBase/service/local/staging/profiles/$stagingProfileId/finish"
  URL(url).openConnection().apply {
    this as HttpURLConnection
    requestMethod = "POST"
    doOutput = true
    addRequestProperty("Content-Type", "application/xml")
    addRequestProperty(
      "Authorization",
      "Basic: ${
        Base64.getEncoder()
          .encodeToString("$username:$password".toByteArray(Charset.defaultCharset()))
      }"
    )
    PrintWriter(outputStream).use { output ->
      output.write(
        """
          <promoteRequest>
    <data>
        <stagedRepositoryId>$repoId</stagedRepositoryId>
        <description>$description</description>
    </data>
</promoteRequest>""".trimIndent()
      )
    }

    log("RESPONSE: $responseCode : $responseMessage")
    if (responseCode != HttpURLConnection.HTTP_CREATED)
      throw Error("Response code: $responseCode $responseMessage")

  }
}

internal fun Project.createCloseRepoTask(extn: SonatypeExtension) {
  if (rootProject.tasks.findByPath("sonatypeCloseRepository") != null) return
  rootProject.tasks.register("sonatypeCloseRepository") {
    description =
      "Closes the sonatype repository as specified by the ${SonatypeExtension.REPO_ID} property"
    group = XTRAS_TASK_GROUP
    doLast {
      if (extn.repoID.get().isBlank()) throw Error("SonatypeExtension.repoID not set")

      sonatypeCloseRepository(
        extn.profileID.get(),
        extn.repoID.get(),
        extn.description.get(),
        extn.username.get(),
        extn.password.get(),
        extn.urlBase.get()
      )
    }
  }
}
