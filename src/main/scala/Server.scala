package net.ivoah.vial

import com.sun.net.httpserver._
import java.net.InetSocketAddress

case class Server(router: Router, host: String = "127.0.0.1", port: Int = 8000) {
  private val server = HttpServer.create(new InetSocketAddress(host, port), 0)
  server.createContext("/", router.handler)

  def serve(): Unit = {
    server.start()
    println(s"Listening on $host:$port")
  }
}
