package net.ivoah.vial

import com.sun.net.httpserver._
import java.net.InetSocketAddress

case class Server(router: Router, port: Int = 8000) {
  private val server = HttpServer.create(new InetSocketAddress(port), 0)
  server.createContext("/", router.handler)
  server.setExecutor(null) // creates a default executor

  def serve(): Unit = server.start()
}
