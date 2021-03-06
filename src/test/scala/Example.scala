import net.ivoah.vial._

object Example extends App {
  val server = Server(Router {
    case ("GET", "/", request) => Response(
      s"""<p>
         |${Seq("/", "/hello", "/hello/$name", "/bye").map(endpoint => s"<a href=\"$endpoint\">$endpoint</a>").mkString("<br>")}
         |</p>
         |${request.params.map { case k -> v => s"\"$k\" is \"$v\"" }.mkString("\n")}
         |""".stripMargin)
    case ("GET", "/hello", _) => Response("Hello!")
    case ("GET", s"/hello/$name", _) => Response(s"Hello $name!")
    case ("GET", "/bye", _) => Response("Goodbye :)")
    case ("GET", s"/favicon.ico", _) => Response.fromResource(s"/favicon.ico")
    case ("GET", "/auth", request) => request.auth match {
      case Some((username, password)) if username == "foo" && password == "bar" => Response("Authenticated!")
      case _ => Response.Unauthorized()
    }
  })
  server.serve()
}
