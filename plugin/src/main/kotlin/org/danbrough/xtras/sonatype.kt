package org.danbrough.xtras

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.w3c.dom.Element
import java.io.InputStream
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

internal fun Project.registerSonatypeOpenRepository(): TaskProvider<Task> =
  tasks.register("sonatypeOpenRepository") {
    description = """
      Open a new sonatype repository and prints it to stdout.
      Specify the repository description with -P${Xtras.Constants.Properties.SONATYPE_DESCRIPTION}="..".
      """.trimMargin()
    group = XTRAS_TASK_GROUP

    val repoIDFile = rootProject.layout.buildDirectory.file("sonatype_repo_id_${group}_${name}")
    outputs.file(repoIDFile)

    doFirst {
      logError("getting sonatype repo ID")
    }

    actions.add {
      logError("doing repo id action")
      val repoFile = repoIDFile.get().asFile
      if (!repoFile.exists()) {
        val response = sonatypeOpenRepository(
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_PROFILE_ID) { error("${Xtras.Constants.Properties.SONATYPE_PROFILE_ID} not set") },
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_DESCRIPTION) { "" },
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_USERNAME) { error("${Xtras.Constants.Properties.SONATYPE_USERNAME} not set") },
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_PASSWORD) { error("${Xtras.Constants.Properties.SONATYPE_PASSWORD} not set") },
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_BASE_URL) { "https://s01.oss.sonatype.org" },
        )

        logDebug("sonatypeResponse: $response")
        repoFile.printWriter().use {
          it.println(response.repositoryId)
        }
      }
    }


  }


private fun sonatypeOpenRepository(
  stagingProfileId: String,
  description: String,
  username: String,
  password: String,
  urlBase: String
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

private data class PromoteRequestResponse(val repositoryId: String, val description: String)

private fun parsePromoteRequest(input: InputStream): PromoteRequestResponse {/*
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
