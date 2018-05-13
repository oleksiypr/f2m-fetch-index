package op.assessment.ftm

import akka.actor.{Actor, Props}
import op.assessment.ftm.CacheActor._

object CacheActor {

  def props: Props = Props[CacheActor]

  sealed trait Query
  sealed trait Command

  sealed trait QueryResult

  final case class GetItem(i: Int) extends Query
  final case class UpdateCache(items: Seq[Compressed[String]]) extends Command

  final case class ItemFound(item: String) extends QueryResult
  final case object ItemNotFound extends QueryResult
}

class CacheActor extends Actor with Compressor {

  var cache: Seq[Compressed[String]] = Seq.empty

  val receive: Receive = {
    case GetItem(index) =>
      val decompressed = decompress(cache)
      if (index >= decompressed.size) {
        sender ! ItemNotFound
      } else {
        sender ! ItemFound(decompressed(index))
      }

    case UpdateCache(values) => cache = values
  }
}