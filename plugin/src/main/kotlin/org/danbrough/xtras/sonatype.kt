package org.danbrough.xtras


import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.core.extra
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.w3c.dom.Element
import java.io.InputStream
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

internal fun Project.registerSonatypeTasks() {
  registerSonatypeOpenRepository()
  registerSonatypeCloseRepository()
}


private fun Project.registerSonatypeOpenRepository() {

  tasks.register(Xtras.Constants.TaskNames.SONATYPE_OPEN_REPO) {
    description = """
      Open a new sonatype repository and prints it to stdout.
      Specify the repository description with -P${Xtras.Constants.Properties.SONATYPE_DESCRIPTION}="..".
      """.trimMargin()
    group = XTRAS_TASK_GROUP

    val repoIDFile = xtrasExtension.repoIDFile
    outputs.file(repoIDFile)

    onlyIf {
      !repoIDFile.get().asFile.exists()
    }

    actions.add {
      val repoFile = repoIDFile.get().asFile

      if (xtrasProperty<String?>(Xtras.Constants.Properties.SONATYPE_REPO_ID) == null) {
        logDebug("getting sonatype repo ID")

        if (!repoFile.exists()) {

          val response = sonatypeOpenRepository(
            xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_PROFILE_ID) { error("${Xtras.Constants.Properties.SONATYPE_PROFILE_ID} not set") },
            xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_DESCRIPTION) {
              "${project.group}:${project.name}:${project.version}".also {
                logWarn("using default sonatype.description: $it")
              }
            },
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
  }
}

private fun Project.registerSonatypeCloseRepository() {
  tasks.register(Xtras.Constants.TaskNames.SONATYPE_CLOSE_REPO) {
    group = XTRAS_TASK_GROUP
    description = "Closes the sonatype repository. If it's been opened"

    doLast {
      val explicitRepoID = xtrasProperty<String?>(Xtras.Constants.Properties.SONATYPE_REPO_ID)
      val repoID =
        explicitRepoID
          ?: xtrasExtension.repoIDFile.get().asFile.let {
            if (it.exists()) it.readText().trim() else null
          }


      if (repoID != null) {
        val stagingProfileID =
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_PROFILE_ID) { error("${Xtras.Constants.Properties.SONATYPE_PROFILE_ID} not set") }
        val description =
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_DESCRIPTION) { "${project.group}:${project.name}:${project.version}" }
        val username =
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_USERNAME) { error("${Xtras.Constants.Properties.SONATYPE_USERNAME} not set") }
        val password =
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_PASSWORD) { error("${Xtras.Constants.Properties.SONATYPE_PASSWORD} not set") }
        val baseURL =
          xtrasProperty<String>(Xtras.Constants.Properties.SONATYPE_BASE_URL) { "https://s01.oss.sonatype.org" }

        logInfo("$name closing repo.. repoID $repoID description:$description")

        sonatypeCloseRepository(
          stagingProfileID,
          repoID,
          description,
          username,
          password,
          baseURL
        )

        if (explicitRepoID == null) {
          val repoIDFile = xtrasExtension.repoIDFile.get().asFile
          logInfo("deleting $repoIDFile")
          repoIDFile.delete()
        }
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
  URI(url).toURL().openConnection().apply {
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


private fun Project.sonatypeCloseRepository(
  stagingProfileId: String?,
  repoId: String?,
  description: String?,
  username: String?,
  password: String?,
  urlBase: String
) {
  log("sonatypeCloseRepository()")
  val url = "$urlBase/service/local/staging/profiles/$stagingProfileId/finish"
  URI(url).toURL().openConnection().apply {
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
          </promoteRequest>
        """.trimIndent()
      )
    }

    log("RESPONSE: $responseCode : $responseMessage")
    if (responseCode != HttpURLConnection.HTTP_CREATED)
      throw Error("Response code: $responseCode $responseMessage")

  }
}

