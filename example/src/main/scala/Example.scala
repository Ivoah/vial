import net.ivoah.vial.*
import scalatags.Text.all.*

object Example {
  @main
  def main(): Unit = {
    val router = Router {
      case ("GET", "/", request) => Response("<!DOCTYPE html>\n" + html(
        head(tag("title")("Vial")),
        body(
          p(
            for (endpoint <- Seq(
              "/",
              "/hello",
              "/hello/$name",
              "/bye", "/auth",
              "/params?foo=bar&baz=biff",
              "/cookies"
            )) yield frag(
              a(href:=endpoint, endpoint), br()
            )
          )
        )
      ))
      case ("GET", "/hello", _) => Response("Hello!")
      case ("GET", s"/hello/$name", _) => Response(s"Hello $name!")
      case ("GET", "/bye", _) => Response("Goodbye :)")
      case ("GET", s"/favicon.ico", _) => Response.fromResource(s"/favicon.ico")
      case ("GET", "/auth", request) => request.auth match {
        case Some((username, password)) if username == "foo" && password == "bar" => Response("Authenticated!")
        case _ => Response.Unauthorized()
      }
      case ("GET", "/params", request) => Response("<!DOCTYPE html>\n" + html(
        head(tag("title")("Params")),
        body(
          h3("Params"),
          ul(
            request.params.toSeq.map{case (k, v) => li(s"${k}: ${v}")}
          )
        )
      ))
      case ("GET", "/cookies", request) => Response("<!DOCTYPE html>\n" + html(
        head(tag("title")("Cookies")),
        body(
          h3("Cookies"),
          ul(
            li(form(action:="/set_cookie", method:="POST",
              input(`type`:="text", name:="name"), ": ", input(`type`:="text", name:="value"), input(`type`:="submit", value:="set cookie")
            )),
            request.cookies.map(c => li(
              form(display:="inline", action:="/delete_cookie", method:="POST",
                input(`type`:="hidden", name:="name", value:=c.name),
                input(`type`:="submit", value:="╳")
              ), " ", s"${c.name}: ${c.value}"
            ))
          )
        )
      ))
      case ("POST", "/set_cookie", request) => Response.Redirect("/cookies").withCookie(Cookie(request.form("name"), request.form("value")))
      case ("POST", "/delete_cookie", request) => Response.Redirect("/cookies").withCookie(Cookie(request.form("name"), "", maxAge = Some(0)))
    }
    val server = Server(router)
    server.serve()
  }
}
