package net.ivoah.vial

import com.sun.net.httpserver._
import java.net.InetSocketAddress

object HttpServerTest extends App {

  class MyHandler extends HttpHandler {
    override def handle(t: HttpExchange): Unit = {
      val response =
        s"""This is the response
           |${t.getRequestURI}
           |${t.getRequestHeaders.entrySet()}
           |""".stripMargin
      t.sendResponseHeaders(200, response.length)
      val os = t.getResponseBody
      os.write(response.getBytes)
      os.close()
    }
  }

  val server = HttpServer.create(new InetSocketAddress(8000), 0)
  server.createContext("/", new MyHandler)
  server.setExecutor(null) // creates a default executor
  server.start()
}
