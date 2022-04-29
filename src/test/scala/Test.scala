package net.ivoah.vial

object Test extends App {
  val server = Server(Router {
    case ("GET", "/", request) => Response(
      s"""Hello there
         |${request.params.map { case k -> v => s"\"$k\" is \"$v\"" }.mkString("\n")}
         |""".stripMargin)
    case ("GET", s"/hello", _) => Response("Hello!")
    case ("GET", s"/hello/$name", _) => Response(s"Hello $name!")
    case ("GET", "/bye", _) => Response("Goodbye :)")
  })
  server.serve()
}
