package net.ivoah.vial

import java.util.Base64
import java.net.URLDecoder
import java.io.File
import java.nio.file.Files

case class Request(headers: Map[String, Seq[String]], params: Map[String, String], body: Array[Byte]) {
  lazy val form: Map[String, String | File] = headers.get("Content-Type") match {
    case Some(Seq("application/x-www-form-urlencoded")) =>
      String(body).split("&").collect {
        case s"$key=$value" => key -> URLDecoder.decode(value, "UTF-8")
      }.toMap
    case Some(Seq(s"""multipart/form-data; boundary="$boundary"""")) => Request.parseMultiPartForm(s"--$boundary", body)
    case Some(Seq(s"multipart/form-data; boundary=$boundary")) => Request.parseMultiPartForm(s"--$boundary", body)
    case _ => Map()
  }

  lazy val auth: Option[(String, String)] = headers.get("Authorization").map {
    case Seq(s"Basic $value") => new String(Base64.getDecoder.decode(value)) match {
      case s"$username:$password" => username -> password
    }
  }

  lazy val cookies: Seq[Cookie] = headers.getOrElse("Cookie", Seq()).flatMap(_.split("; ")).map {
    case s"$name=$value" => Cookie(name, value)
  }
}

object Request {
  private def parseMultiPartForm(boundary: String, body: Array[Byte]): Map[String, String | File] = {
    Iterator.unfold(boundary.length + 2) { i =>
      body.indexOfSlice(boundary, i) match {
        case -1 => None
        case n => Some(body.slice(i, n - 2), n + boundary.length + 2)
      }
    }.map { bytes =>
      val headersEnd = bytes.indexOfSlice("\r\n\r\n")
      val headers = String(bytes.take(headersEnd)).split("\r\n")
      val content = bytes.drop(headersEnd + 4)
      headers.collect {
        case s"""Content-Disposition: form-data; name="$name"$rest""" =>
          name -> (rest match {
            case s"""; filename=""""" => null
            case s"""; filename="$filename"""" =>
              val path = Files.createTempFile("", filename)
              Files.write(path, content).toFile
            case _ => String(content)
          })
      }.head
    }.toMap
  }
}
