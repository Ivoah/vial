package net.ivoah.vial

import jakarta.servlet.http._
import org.eclipse.jetty.server._
import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.util.thread.QueuedThreadPool

import java.net.InetSocketAddress
import java.io.File

/** Main entry point of a vial application.
  *
  * Takes either a host/port combo, or the path to a unix domain socket.
  * 
  * @param router the router to use
  * @param host host address to bind to, defaults to "127.0.0.1", ignored if `socket` is present.
  * @param port port to bind to, defaults to 8000, ignored if `socket` is present.
  * @param socket path to unix domain socket, optional.
  * @param logger
  */
case class Server(router: Router, host: String = "127.0.0.1", port: Int = 8000, socket: Option[String] = None)(implicit val logger: String => Unit = println) {

  // Create and configure a ThreadPool.
  private val threadPool = new QueuedThreadPool
  threadPool.setName("server")

  // Create a Server instance.
  private val server = new org.eclipse.jetty.server.Server(threadPool)

  // Create a Connector to accept connections from clients.
  private val connector = socket match {
    case Some(path) =>
      val socket = new File(path)
      if (socket.exists()) {
        Console.err.println(s"Socket $path exists, exiting")
        sys.exit(1)
      }
      socket.deleteOnExit()
      val connector = new UnixDomainServerConnector(server)
      connector.setUnixDomainPath(socket.toPath)
      connector
    case None =>
      val connector = new ServerConnector(server)
      connector.setHost(host)
      connector.setPort(port)
      connector
  }

  // Add the Connector to the Server
  server.addConnector(connector)

  // Set a simple Handler to handle requests/responses.
  server.setHandler(router.handler)

  /** Start the server.
    * This will block while the server is running.
    */
  def serve(): Unit = {
    server.start()
    socket match {
      case Some(path) => logger(s"Listening on unix socket ${path}")
      case None => logger(s"Listening on http://$host:$port")
    }
  }
}
