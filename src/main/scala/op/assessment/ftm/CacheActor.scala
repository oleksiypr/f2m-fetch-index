package op.assessment.ftm

import akka.actor.Actor
import op.assessment.ftm.CacheActor.{GetItem, Items}

object CacheActor {
  final case class GetItem(i: Int)
  final case class Item(item: String)
  final case class Items(items: Seq[Compressed[String]])
}

class CacheActor extends Actor with Compressor {

  var cache: Seq[Compressed[String]] = Seq.empty

  val receive: Receive = {
    case GetItem(index) => decompress(cache)(index)
    case Items(values) => cache = values
  }
}