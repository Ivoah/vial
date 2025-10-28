package net.ivoah.vial

import java.nio.file.*
import java.nio.file.spi.FileSystemProvider
import scala.jdk.CollectionConverters.*

import Extensions.*

val BUFFER_SIZE = 1024*8

case class Response(data: IterableOnce[Array[Byte]], headers: Map[String, Seq[String]] = Map(), cookies: Seq[Cookie] = Seq(), statusCode: Int = 200) {
  def withCookie(cookie: Cookie): Response = Response(
    data = data,
    headers = headers,
    cookies = cookies :+ cookie,
    statusCode = statusCode
  )
}

object Response {
  def apply(content: String): Response = Response(Seq(content.getBytes), headers = Map("Content-Type" -> Seq("text/html; charset=UTF-8")))
  def apply(content: String, headers: Map[String, Seq[String]]): Response = Response(Seq(content.getBytes), headers)
  def apply(content: String, status_code: Int): Response = Response(Seq(content.getBytes), headers = Map("Content-Type" -> Seq("text/html; charset=UTF-8")), statusCode = status_code)
  def apply(content: String, headers: Map[String, Seq[String]], status_code: Int): Response = Response(Seq(content.getBytes), headers, Seq(), status_code)

  def forFile(root: Path, file: Path, mime: Option[String] = None, headers: Map[String, Seq[String]] = Map()): Response = {
    // Restrict files to be underneath root
    if (Files.exists(root.resolve(file)) && root.resolve(file).toRealPath(LinkOption.NOFOLLOW_LINKS).startsWith(root.toRealPath(LinkOption.NOFOLLOW_LINKS))) {
      val path = root.resolve(file)
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

      Response(new Iterator[Array[Byte]] {
        private val stream = Files.newInputStream(path)
        private val buffer = Array.ofDim[Byte](BUFFER_SIZE)
        override def hasNext: Boolean = stream.available() > 0
        override def next(): Array[Byte] = {
          val n = stream.read(buffer)
          buffer.take(n)
        }
      }, headers = Map("Content-Type" -> Seq(mime.getOrElse(Files.probeContentType(path)))) ++ headers)
    } else NotFound()
  }

//  def fromResource(name: String): Response = Option(getClass.getResource(name)) match {
//    case Some(url) => forFile(Paths.get(url.toURI))
//    case None => NotFound()
//  }

  def json[T: upickle.Writer](value: T, status_code: Int = 200): Response = Response(upickle.write(value), headers = Map("Content-Type" -> Seq("application/json")), status_code = status_code)

  def Redirect(url: String): Response = Response(s"303 See Other", headers = Map("Location" -> Seq(url)), status_code = 303)
  def BadRequest(msg: String = ""): Response = Response(s"400 Bad Request\n$msg", headers = Map("Content-Type" -> Seq("text/plain; charset=UTF-8")), status_code = 400)
  def Unauthorized(realm: String = "private"): Response = Response("401 Unauthorized", headers = Map("Content-Type" -> Seq("text/plain; charset=UTF-8"), "WWW-Authenticate" -> Seq(s"Basic realm=$realm")), status_code = 401)
  def Forbidden(msg: String = ""): Response = Response(s"403 Forbidden\n$msg", headers = Map("Content-Type" -> Seq("text/plain; charset=UTF-8")), status_code = 403)
  def NotFound(msg: String = ""): Response = Response(s"404 Not Found\n$msg", headers = Map("Content-Type" -> Seq("text/plain; charset=UTF-8")), status_code = 404)
  def ImATeapot(msg: String = ""): Response = Response(s"418 I'm a teapot\n$msg", headers = Map("Content-Type" -> Seq("text/plain; charset=UTF-8")), status_code = 418)
  def InternalServerError(msg: String | Exception = ""): Response = Response(
    s"500 internal server error\n${msg match {
      case s: String => s
      case e: Exception => s"\n$e\n${e.getStackTrace.map("... " + _).mkString("\n")}"
    }}",
    headers = Map("Content-Type" -> Seq("text/plain; charset=UTF-8")),
    status_code = 500
  )
}
