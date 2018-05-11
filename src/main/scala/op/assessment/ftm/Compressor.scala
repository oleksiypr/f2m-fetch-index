package op.assessment.ftm

trait Compressor {

  def compress[A](as: Seq[A]): Seq[Compressed[A]] = {
    as.foldLeft(Seq.empty[Compressed[A]]) { (comp, a) =>
      comp.lastOption map {
        case Single(e) if e != a => comp :+ Single(a)
        case Single(e) => comp.init :+ Repeat(count = 2, a)
        case Repeat(_, e) if e != a => comp :+ Single(a)
        case Repeat(n, e) => comp.init :+ Repeat(n + 1, e)
      } getOrElse {
        Seq(Single(a))
      }
    }
  }

  def decompress[A](as: Seq[Compressed[A]]): Seq[A] = {
    as flatMap {
      case Single(e) => Seq(e)
      case Repeat(n, e) => Seq.fill(n)(e)
    }
  }
}

sealed trait Compressed[+A]
case class Single[A](element: A) extends Compressed[A]
case class Repeat[A](count: Int, element: A) extends Compressed[A]
