package net.ivoah.vial

import scala.jdk.CollectionConverters._
import com.sun.net.httpserver.{HttpHandler, HttpExchange}

case class Router(routes: PartialFunction[(String, String, Request), Response], staticRoutes: Map[String, String] = Map()) {
  val handler: HttpHandler = (t: HttpExchange) => {
    val response = try {
      println(s"${t.getRequestMethod} ${t.getRequestURI.getPath}")

      val request = Request(
        headers = t.getRequestHeaders.asScala.map {
          case (key, value) => key -> value.asScala.toSeq
        }.toMap,
        params = Option(t.getRequestURI.getQuery).getOrElse("").split('&').collect {
          case s"$key=$value" => key -> value
        }.toMap,
        t.getRequestBody.readAllBytes()
      )

      routes.lift((t.getRequestMethod, t.getRequestURI.getPath, request)).getOrElse(Response.NotFound())
    } catch {
      case e: Exception =>
        e.printStackTrace()
        Response.InternalServerError(e)
    }

    println(s"  => ${response.status_code}")

    response.headers.foreach{case (k, vs) => vs.foreach(v => t.getResponseHeaders.add(k, v))}
//    t.sendResponseHeaders(response.status_code, response.data.length)
    t.sendResponseHeaders(response.status_code, 0)
    val os = t.getResponseBody
    os.write(response.data)
    t.close()
  }
}
