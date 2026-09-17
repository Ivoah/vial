package net.ivoah.vial

import java.nio.charset.Charset

case class ContentType(mime: String, charset: Option[Charset], boundary: Option[String])

object ContentType {
  def fromHeader(header: String): ContentType = header match {
    case s"$mime; charset=$charset" => ContentType(mime, Some(Charset.forName(charset)), None)
    case s"""$mime; boundary="$boundary"""" => ContentType(mime, None, Some(boundary))
    case s"$mime; boundary=$boundary" => ContentType(mime, None, Some(boundary))
    case mime => ContentType(mime, None, None)
  }
}
