import net.ivoah.vial.*
import scalatags.Text.all.*
import java.nio.file.Paths

object Example {
  @main
  def main(): Unit = {
    val router = Router {
      case ("GET", "/", request) => Response("<!DOCTYPE html>\n" + html(
        head(tag("title")("Vial")),
        body(
          img(src:="/static/vial.png", width:=64),
          ul(
            for (endpoint <- Seq(
              "/",
              "/hello",
              "/hello/$name",
              "/bye", "/auth",
              "/params?foo=bar&baz=biff",
              "/cookies"
            )) yield frag(
              li(a(href:=endpoint, endpoint))
            ),
            li(form(action:="/form", method:="post", input(name:="input"), button("submit"))),
            li(form(action:="/form", method:="post", enctype:="multipart/form-data",
              input(name:="text"),
              input(`type`:="file", name:="file"),
              button("upload")
            ))
          )
        )
      ))
      case ("GET", "/hello", _) => Response("Hello!")
      case ("GET", s"/hello/$name", _) => Response(s"Hello $name!")
      case ("GET", "/bye", _) => Response("Goodbye :)")
//      case ("GET", s"/favicon.ico", _) => Response.fromResource(s"/favicon.ico")
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
      case ("POST", "/set_cookie", request) => Response.Redirect("/cookies").withCookie(Cookie(request.form("name").asInstanceOf[String], request.form("value").asInstanceOf[String]))
      case ("POST", "/delete_cookie", request) => Response.Redirect("/cookies").withCookie(Cookie(request.form("name").asInstanceOf[String], "", maxAge = Some(0)))
      case ("POST", "/form", request) => Response("<!DOCTYPE html>\n" + html(
        head(tag("title")("Form")),
        body(
          h3("Form"),
          ul(
            request.form.toSeq.map{case (k, v) => li(s"${k}: ${v}")}
          )
        )
      ))
      case ("POST", "/formExpect", request) => request.form.expect("foo", "bar") { (foo: String, bar: String) => Response("<!DOCTYPE html>\n" + html(
        p(s"Got foo=$foo and bar=$bar as expected")
      ))}
      case ("GET", s"/static/$file", _) => Response.forFile(Paths.get("static"), Paths.get(file))

      case ("GET", "/stream", _) => Response(new Iterator[Array[Byte]] {
        private var i = 0
        override def hasNext: Boolean = i < 100
        override def next(): Array[Byte] = {
          Thread.sleep(100)
          i += 1
          s"$i\n".getBytes
        }
      })
    }
    val server = Server(router, ("localhost", 8081))
    server.serve()
  }
}
