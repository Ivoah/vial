package net.ivoah.vial

import scala.jdk.CollectionConverters.*
import java.net.URLDecoder
import jakarta.servlet.http.*
import org.eclipse.jetty.server.{Request as JettyRequest, Response as JettyResponse}
import org.eclipse.jetty.server.handler.AbstractHandler

import scala.annotation.targetName

case class Router(routes: PartialFunction[(String, String, Request), Response]) {
  val handler: AbstractHandler = new AbstractHandler() {
    def handle(target: String, jettyRequest: JettyRequest, srequest: HttpServletRequest, sresponse: HttpServletResponse): Unit = {
      val response = try {
        val uri = URLDecoder.decode(srequest.getRequestURI, "UTF-8")
        println(s"${srequest.getMethod} ${uri}")

        val request = Request(
          headers = srequest.getHeaderNames.asScala.map(header => header -> srequest.getHeaders(header).asScala.toSeq).toMap,
          params = Option(srequest.getQueryString).getOrElse("").split('&').collect {
            case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8")
          }.toMap,
          srequest.getInputStream.readAllBytes()
        )

        routes.lift((srequest.getMethod, uri, request)).getOrElse(Response.NotFound())
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Response.InternalServerError(e)
      }

      println(s"  => ${response.status_code}")

      response.headers.foreach { case (k, vs) => vs.foreach(v => sresponse.setHeader(k, v)) }
      sresponse.setStatus(response.status_code)
      val os = sresponse.getOutputStream
      os.write(response.data)
      os.close()
    }
  }

  def ++(other: Router): Router = Router(routes.orElse(other.routes))
}
