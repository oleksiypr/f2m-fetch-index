package op.assessment.ftm

import akka.actor.{Actor, ActorRef}
import op.assessment.ftm.CacheActor.UpdateCache
import op.assessment.ftm.FetchActor.Fetch

object FetchActor {
  case object Fetch
}

trait ItemsFetcher extends Compressor {
  def fetch(): Seq[String]
  def fetchCompressed(): Seq[Compressed[String]] = {
    compress(fetch())
  }
}

trait NetworkItemsFetcher extends ItemsFetcher {
  override def fetch(): Seq[String] = ???
}

trait FetchActor extends Actor with ItemsFetcher {

  val cacheActor: ActorRef

  val receive: Receive = {
    case Fetch =>
      val fetched = fetchCompressed()
      cacheActor ! UpdateCache(fetched)
  }
}

class NetworkFetchActor(
    override val cacheActor: ActorRef
  ) extends FetchActor with NetworkItemsFetcher

