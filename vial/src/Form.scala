package net.ivoah.vial

import java.net.URLDecoder
import java.io.File
import java.nio.file.Files
import java.nio.charset.Charset

class Form(request: Request) {
  val data: Seq[(String, String | File)] = request.headers.get("Content-Type").flatMap(_.headOption).map(ContentType.fromHeader) match {
    case Some(ContentType("application/x-www-form-urlencoded", charset, _)) =>
      String(request.body, charset.getOrElse(Charset.defaultCharset())).split("&").toSeq.collect {
        case s"$key=$value" => URLDecoder.decode(key, "UTF-8") -> URLDecoder.decode(value, "UTF-8")
      }
    case Some(ContentType("multipart/form-data", _, Some(boundary))) =>
      val delimiter = s"--$boundary"
      Iterator.unfold(delimiter.length + 2) { i =>
        request.body.indexOfSlice(delimiter, i) match {
          case -1 => None
          case n => Some(request.body.slice(i, n - 2), n + delimiter.length + 2)
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
      }.toSeq
    case _ => Seq()
  }

  def get[T: FormValueParser](key: String): Option[T] = data.find(_._1 == key).map(_._2).flatMap(FormValueParser.parse)
  def getOrElse[T: FormValueParser](key: String, default: => T): T = get(key).getOrElse(default)

  def getAll[T: FormValueParser](key: String): Seq[T] = data.collect {
    case (k, v) if k == key => FormValueParser.parse(v)
  }.flatten

  def expect[T1: FormValueParser, R](k1: String)(fn: T1 => R): Option[R] = {
    for (p1 <- get[T1](k1))
    yield fn(p1)
  }

  def expect[T1: FormValueParser, T2: FormValueParser, R](k1: String, k2: String)(fn: (T1, T2) => R): Option[R] = {
    for (p1 <- get[T1](k1); p2 <- get[T2](k2))
     yield fn(p1, p2)
  }

  def expect[T1: FormValueParser, T2: FormValueParser, T3: FormValueParser, R](k1: String, k2: String, k3: String)(fn: (T1, T2, T3) => R): Option[R] = {
    for (p1 <- get[T1](k1); p2 <- get[T2](k2); p3 <- get[T3](k3))
     yield fn(p1, p2, p3)
  }

  def expect[T1: FormValueParser, T2: FormValueParser, T3: FormValueParser, T4: FormValueParser, R](k1: String, k2: String, k3: String, k4: String)(fn: (T1, T2, T3, T4) => R): Option[R] = {
    for (p1 <- get[T1](k1); p2 <- get[T2](k2); p3 <- get[T3](k3); p4 <- get[T4](k4))
    yield fn(p1, p2, p3, p4)
  }

  def expect[T1: FormValueParser, T2: FormValueParser, T3: FormValueParser, T4: FormValueParser, T5: FormValueParser, R](k1: String, k2: String, k3: String, k4: String, k5: String)(fn: (T1, T2, T3, T4, T5) => R): Option[R] = {
    for (p1 <- get[T1](k1); p2 <- get[T2](k2); p3 <- get[T3](k3); p4 <- get[T4](k4); p5 <- get[T5](k5))
    yield fn(p1, p2, p3, p4, p5)
  }
}
