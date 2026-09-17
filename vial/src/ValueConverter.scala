package net.ivoah.vial

import java.io.File
import scala.reflect.ClassTag

trait ValueConverter[T] {
	def parse(v: String | File): Option[T]
}

object ValueConverter {
	def parse[T: ValueConverter as fp](v: String | File): Option[T] = fp.parse(v)

	extension[V](v: V) private def is[T: ClassTag]: Option[T] = Some(v).collect{case t: T => t}

	given ValueConverter[String]  = _.is[String]
	given ValueConverter[File]    = _.is[File]
	given ValueConverter[Int]     = _.is[String].flatMap(_.toIntOption)
	given ValueConverter[Double]  = _.is[String].flatMap(_.toDoubleOption)
	given ValueConverter[Boolean] = {
		case "true" => Some(true)
		case "false" => Some(false)
		case _ => None
	}
}
