package net.ivoah.vial

import java.util.Base64
import java.net.URLDecoder
import java.io.File
import java.nio.file.Files

extension [K, V](map: Map[K, V]) {
  def expect[T1](p1: K)(fn: T1 => Response): Response = {
    try fn(map(p1).asInstanceOf[T1])
    catch case e: (NoSuchElementException | ClassCastException) => Response.BadRequest()
  }

  def expect[T1, T2](p1: K, p2: K)(fn: (T1, T2) => Response): Response = {
    try fn(map(p1).asInstanceOf[T1], map(p2).asInstanceOf[T2])
    catch case e: (NoSuchElementException | ClassCastException) => Response.BadRequest()
  }

  def expect[T1, T2, T3](p1: K, p2: K, p3: K)(fn: (T1, T2, T3) => Response): Response = {
    try fn(map(p1).asInstanceOf[T1], map(p2).asInstanceOf[T2], map(p3).asInstanceOf[T3])
    catch case e: (NoSuchElementException | ClassCastException) => Response.BadRequest()
  }
}

case class Request(headers: Map[String, Seq[String]], params: Map[String, String], body: Array[Byte]) {
  lazy val form: Map[String, String | File] = headers.get("Content-Type") match {
    case Some(Seq(s"application/x-www-form-urlencoded$_")) =>
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
