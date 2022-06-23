package net.ivoah.vial

import java.nio.file._
import java.nio.file.spi.FileSystemProvider
import scala.jdk.CollectionConverters._

case class Response(
                     data: Array[Byte],
                     headers: Map[String, Seq[String]] = Map(),
                     status_code: Int = 200)

object Response {
  def apply(content: String): Response = Response(content.getBytes)
  def apply(content: String, headers: Map[String, Seq[String]]): Response = Response(content.getBytes, headers)
  def apply(content: String, status_code: Int): Response = Response(content.getBytes, status_code = status_code)
  def apply(content: String, headers: Map[String, Seq[String]], status_code: Int): Response = Response(content.getBytes, headers, status_code)

  def forFile(path: Path, mime: Option[String] = None): Response = {
    val uri = path.toUri
    if (uri.getScheme == "jar") {
      for (provider <- FileSystemProvider.installedProviders.asScala) {
        if (provider.getScheme.equalsIgnoreCase("jar")) try provider.getFileSystem(uri)
        catch {
          case e: FileSystemNotFoundException =>
            // in this case we need to initialize it first:
            provider.newFileSystem(uri, java.util.Collections.emptyMap)
        }
      }
    }

    if (Files.exists(path)) {
      val content = Files.readAllBytes(path)
      Response(content, headers = Map("Content-Type" -> Seq(mime.getOrElse(Files.probeContentType(path)))))
    } else {
      NotFound()
    }
  }

  def Redirect(url: String): Response = Response(s"303 see other", headers = Map("Location" -> Seq(url)), status_code = 303)
  def Unauthorized(realm: String = "private"): Response = Response("401 unauthorized", headers = Map("WWW-Authenticate" -> Seq(s"Basic realm=$realm")), status_code = 401)
  def NotFound(msg: String = ""): Response = Response(s"404 not found\n$msg", status_code = 404)
  def InternalServerError(e: Exception): Response = Response(s"""500 internal server error
                                                                |
                                                                |${e.toString}
                                                                |${e.getStackTrace.map("... " + _).mkString("\n")}
                                                                |""".stripMargin, status_code = 500)
}
