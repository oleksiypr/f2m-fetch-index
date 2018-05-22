package op.assessment.ftm

import akka.actor.{Actor, Props}
import akka.event.Logging
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

  final case class Cache[A](compressed: Vector[Compressed[A]]) {

    private[this] val ns = (compressed map {
      case Repeat(n, _) => n
      case Single(_) => 1
    }).scanLeft(0)(_+_).drop(1)

    private[this] val values = compressed map {
      case Single(e) => e
      case Repeat(_, e) => e
    }

    def apply(index: Int): A = {
      import scala.collection.Searching._
      values(ns.search(index + 1).insertionPoint)
    }

    val size: Int = (compressed map {
      case Single(_) => 1
      case Repeat(n, _) => n
    }).sum
  }
}

class CacheActor extends Actor with Compressor {

  val log = Logging(context.system, this)

  var cache: Cache[String] = Cache(Vector.empty[Compressed[String]])

  val receive: Receive = {
    case GetItem(index) =>
      if (index >= cache.size) {
        sender ! ItemNotFound
      } else {
        sender ! ItemFound(cache(index))
      }

    case UpdateCache(values) =>
      cache = Cache(values.toVector)
      log.debug(s"Cache updated: ${cache.size} items")
  }
}