package net.ivoah.vial

private object Extensions {
  extension [A, B](a: Map[A, Seq[B]]) {
    def merge(b: Map[A, Seq[B]]): Map[A, Seq[B]] = (a.keySet union b.keySet).map { k =>
      k -> (a.getOrElse(k, Seq()) ++ b.getOrElse(k, Seq()))
    }.toMap
  }
}
