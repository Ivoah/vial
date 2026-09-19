package net.ivoah.vial

import java.io.File

trait FormExtractor[T] {
  def get(f: Seq[(String, String | File)], k: String): Option[T]
}

object FormExtractor {
  def get[T: FormExtractor as fe](f: Seq[(String, String | File)], k: String): Option[T] = fe.get(f, k)

  given [T: ValueConverter] => FormExtractor[T]         = (f, k) => f.toMap.get(k).flatMap(ValueConverter.parse)
  given [T: FormExtractor]  => FormExtractor[Option[T]] = (f, k) => Some(FormExtractor.get(f, k))
  given [T: ValueConverter] => FormExtractor[Seq[T]]    = (f, k) => {
    val values = f.filter(_._1 == k).map(t => ValueConverter.parse(t._2))
    if (!values.exists(_.isEmpty)) Some(values.map(_.get)) else None
  }
}
