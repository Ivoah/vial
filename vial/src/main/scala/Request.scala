package net.ivoah.vial

import java.util.Base64
import java.net.URLDecoder

case class Request(headers: Map[String, Seq[String]], params: Map[String, String], body: Array[Byte]) {
  lazy val form: Map[String, String] = new String(body).split("&").collect {
    case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8")
  }.toMap

  lazy val auth: Option[(String, String)] = headers.get("Authorization").map {
    case Seq(s"Basic $value") => new String(Base64.getDecoder.decode(value)) match {
      case s"$username:$password" => username -> password
    }
  }

  lazy val cookies: Seq[Cookie] = headers.getOrElse("Cookie", Seq()).flatMap(_.split("; ")).map {
    case s"$name=$value" => Cookie(name, value)
  }
}
