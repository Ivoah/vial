package net.ivoah.vial

import scala.jdk.CollectionConverters.*
import java.net.URLDecoder
import jakarta.servlet.http.*
import org.eclipse.jetty.server.{Request as JettyRequest, Response as JettyResponse}
import org.eclipse.jetty.server.handler.AbstractHandler
import scala.util.Try

case class Router(routes: PartialFunction[(String, String, Request) | (String, String, Request, Throwable), Response]) {
  def handler(debug: Boolean)(implicit logger: String => Unit): AbstractHandler = new AbstractHandler() {
    def handle(target: String, jettyRequest: JettyRequest, srequest: HttpServletRequest, sresponse: HttpServletResponse): Unit = {
      val uri = URLDecoder.decode(srequest.getRequestURI, "UTF-8")
      logger(s"${srequest.getMethod} ${srequest.getRequestURL}${Option(srequest.getQueryString).map(qs => s"?$qs").getOrElse("")}")

      val request = Request(
        method = srequest.getMethod,
        path = uri,
        headers = srequest.getHeaderNames.asScala.map(header => header -> srequest.getHeaders(header).asScala.toSeq).toMap,
        params = Option(srequest.getQueryString).getOrElse("").split('&').collect {
          case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8")
          case bare => bare -> ""
        }.toMap,
        body = srequest.getInputStream.readAllBytes()
      )

      val response = Try(routes.lift((srequest.getMethod, uri, request)).getOrElse(Response.NotFound()))
        .recover(routes.compose(e => (srequest.getMethod, uri, request, e)))
        .recover { e =>
          e.printStackTrace()
          Response.InternalServerError(if (debug) e else "")
        }
        .get

      logger(s"  => ${response.statusCode}")

      response.headers.foreach { case (k, vs) => vs.foreach(v => sresponse.setHeader(k, v)) }
      response.cookies.foreach(c => sresponse.addCookie(c.toServletCookie))
      sresponse.setStatus(response.statusCode)
      val outputStream = sresponse.getOutputStream
      for (chunk <- response.data) {
        outputStream.write(chunk)
        outputStream.flush()
      }
      outputStream.close()
    }
  }

  /** Create new [[Router]] by combining the routes of this and another.
    *
    * Routes from this router take priority over `other`.
    *
    * @param other other [[Router]] to combine with.
    * @return
    */
  def ++(other: Router): Router = Router(routes.orElse(other.routes))
}
