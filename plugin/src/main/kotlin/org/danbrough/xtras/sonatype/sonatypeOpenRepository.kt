package org.danbrough.xtras.sonatype

import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.logDebug
import org.gradle.api.Project
import org.w3c.dom.Element
import java.io.InputStream
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory


internal fun sonatypeOpenRepository(
  stagingProfileId: String, description: String, username: String, password: String, urlBase: String
): PromoteRequestResponse {
  val url = "$urlBase/service/local/staging/profiles/$stagingProfileId/start"
  URL(url).openConnection().apply {
    this as HttpURLConnection
    requestMethod = "POST"
    doOutput = true
    addRequestProperty("Content-Type", "application/xml")
    addRequestProperty(
      "Authorization", "Basic: ${
        Base64.getEncoder()
          .encodeToString("$username:$password".toByteArray(Charset.defaultCharset()))
      }"
    )

    PrintWriter(outputStream).use { output ->
      output.write(
        """<promoteRequest>
    <data>
        <description>$description</description>
    </data>
</promoteRequest>""".trimIndent()
      )
    }

    if (responseCode == HttpURLConnection.HTTP_CREATED) return parsePromoteRequest(inputStream)

    throw Error("Failed: error: $responseCode: $responseCode: $responseMessage")
  }
}

data class PromoteRequestResponse(val repositoryId: String, val description: String)

fun parsePromoteRequest(input: InputStream): PromoteRequestResponse {/*
        <promoteResponse>
        <data>
          <stagedRepositoryId>orgdanbrough-1185</stagedRepositoryId>
          <description></description>
        </data>
      </promoteResponse>

  */


  val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
  return doc.getElementsByTagName("data").item(0).run {
    this as Element
    val repoID = getElementsByTagName("stagedRepositoryId").item(0).textContent
    val description = getElementsByTagName("description").item(0).textContent
    PromoteRequestResponse(repoID, description)
  }

}

internal fun Project.createOpenRepoTask(extn: SonatypeExtension) {
  if (rootProject.tasks.findByPath("sonatypeOpenRepository") != null) return
  rootProject.tasks.register("sonatypeOpenRepository") {
    description = """
      Open a new sonatype repository and prints it to stdout.
      Specify the repository description with -P${SonatypeExtension.DESCRIPTION}="..".
      """.trimMargin()
    group = XTRAS_TASK_GROUP
    doLast {
      if (extn.profileID.get().isBlank()) throw Error("sonatype.stagingProfileId not set")

      val response = sonatypeOpenRepository(
        extn.profileID.get(),
        extn.description.get(),
        extn.username.get(),
        extn.password.get(),
        extn.urlBase.get()
      )

      project.logDebug("Received response: $response")
      println("export sonatype.repoID=${response.repositoryId}")
    }
  }
}
