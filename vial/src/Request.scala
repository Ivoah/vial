package net.ivoah.vial

import java.util.Base64

case class Request(method: String, path: String, headers: Map[String, Seq[String]], params: Map[String, String], body: Array[Byte]) derives upickle.Writer {
  lazy val form: Form = Form(this)

  lazy val auth: Option[(String, String)] = headers.get("Authorization").map {
    case Seq(s"Basic $value") => new String(Base64.getDecoder.decode(value)) match {
      case s"$username:$password" => username -> password
    }
  }

  lazy val cookies: Seq[Cookie] = headers.getOrElse("Cookie", Seq()).flatMap(_.split("; ")).filter(_.nonEmpty).map {
    case s"$name=$value" => Cookie(name, value)
  }
}
