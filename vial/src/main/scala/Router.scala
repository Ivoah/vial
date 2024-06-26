package net.ivoah.vial

import scala.jdk.CollectionConverters.*
import java.net.URLDecoder
import jakarta.servlet.http.*
import org.eclipse.jetty.server.{Request as JettyRequest, Response as JettyResponse}
import org.eclipse.jetty.server.handler.AbstractHandler

import scala.annotation.targetName

case class Router(routes: PartialFunction[(String, String, Request), Response]) {
  def handler(implicit logger: String => Unit): AbstractHandler = new AbstractHandler() {
    def handle(target: String, jettyRequest: JettyRequest, srequest: HttpServletRequest, sresponse: HttpServletResponse): Unit = {
      val response = try {
        val uri = URLDecoder.decode(srequest.getRequestURI, "UTF-8")
        logger(s"${srequest.getMethod} ${srequest.getRequestURL}${Option(srequest.getQueryString).map(qs => s"?$qs").getOrElse("")}")

        val request = Request(
          headers = srequest.getHeaderNames.asScala.map(header => header -> srequest.getHeaders(header).asScala.toSeq).toMap,
          params = Option(srequest.getQueryString).getOrElse("").split('&').collect {
            case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8")
            case bare => bare -> ""
          }.toMap,
          srequest.getInputStream.readAllBytes()
        )

        routes.lift((srequest.getMethod, uri, request)).getOrElse(Response.NotFound())
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Response.InternalServerError(e)
      }

      logger(s"  => ${response.statusCode}")

      response.headers.foreach { case (k, vs) => vs.foreach(v => sresponse.setHeader(k, v)) }
      sresponse.setStatus(response.statusCode)
      val os = sresponse.getOutputStream
      os.write(response.data)
      os.close()
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
