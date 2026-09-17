package net.ivoah.vial

import java.io.File
import scala.reflect.ClassTag

trait FormValueParser[T] {
  def parse(v: String | File): Option[T]
}

object FormValueParser {
  def parse[T](v: String | File)(using fp: FormValueParser[T]): Option[T] = fp.parse(v)

  extension[V](v: V) {
    private def is[T: ClassTag]: Option[T] = Some(v).collect{case t: T => t}
  }

  given FormValueParser[String]  = _.is[String]
  given FormValueParser[File]    = _.is[File]
  given FormValueParser[Int]     = _.is[String].flatMap(_.toIntOption)
  given FormValueParser[Double]  = _.is[String].flatMap(_.toDoubleOption)
  given FormValueParser[Boolean] = _ match {
    case "true" => Some(true)
    case "false" => Some(false)
    case _ => None
  }

}
