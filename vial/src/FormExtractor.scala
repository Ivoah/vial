package net.ivoah.vial

import java.io.File

trait FormExtractor[T] {
  def get(f: Seq[(String, String | File)], k: String): Option[T]
}

object FormExtractor {
  def get[T: FormExtractor as fe](f: Seq[(String, String | File)], k: String): Option[T] = fe.get(f, k)

  given [T: ValueConverter] => FormExtractor[Seq[T]] {
    def get(f: Seq[(String, String | File)], k: String): Option[Seq[T]] = {
      val values = f.collect {
        case (key, v) if key == k => ValueConverter.parse(v)
      }.flatten
      if (values.nonEmpty) Some(values) else None
    }
  }

  given [T: FormExtractor as fe] => FormExtractor[Option[T]] = (k, v) => Some(fe.get(k, v))
  given [T: ValueConverter] => FormExtractor[T] = (f, k) => f.toMap.get(k).flatMap(ValueConverter.parse)
}
