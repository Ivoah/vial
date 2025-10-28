package net.ivoah.vial

import java.time.Instant
import jakarta.servlet.http.{Cookie => JCookie}

enum SameSite(val value: String) {
  case Strict extends SameSite("Strict")
  case Lax extends SameSite("Lax")
  case None extends SameSite("None")
}

case class Cookie(
                   name: String,
                   value: String,
                   domain: Option[String] = None,
                   expires: Option[Instant] = None,
                   httpOnly: Option[Boolean] = None,
                   maxAge: Option[Int] = None,
                   partitioned: Option[Boolean] = None,
                   path: Option[String] = None,
                   sameSite: Option[SameSite] = None
                 ) {
  def header: Map[String, Seq[String]] = {
    Map("Set-Cookie" -> Seq(
      s"$name=$value"
        + domain.map(d => s"; Domain=$d").getOrElse("")
        // FIXME: Don't use .toString, not the right format for the header
        + expires.map(e => s"; Expires=${e.toString}").getOrElse("")
        + (if (httpOnly.exists(identity)) "; HttpOnly" else "")
        + maxAge.map(ma => s"; Max-Age=$ma").getOrElse("")
        + (if (partitioned.exists(identity)) "; Partitioned" else "")
        + path.map(p => s"; Path=$p").getOrElse("")
        + sameSite.map(ss => s"; SameSite=${ss.value}").getOrElse("")
    ))
  }

  def toServletCookie: JCookie = {
    val c = JCookie(name, value)
    domain.foreach(c.setDomain)
    httpOnly.foreach(c.setHttpOnly)
    maxAge.foreach(c.setMaxAge)
    path.foreach(c.setPath)
    c
  }
}
