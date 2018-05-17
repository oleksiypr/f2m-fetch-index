package op.assessment.ftm

import akka.actor.{Actor, ActorRef, Props}
import op.assessment.ftm.CacheActor.UpdateCache
import op.assessment.ftm.FetchActor.Fetch
import scala.concurrent.duration.{FiniteDuration, _}

object FetchActor {

  def props(cacheActor: ActorRef): Props = Props(
    new NetworkFetchActor(cacheActor)
  )

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

  val interval: FiniteDuration = {
    val config = context.system.settings.config
    config.getDouble("fetch.interval.seconds")
  }.seconds

  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.schedule(0.seconds, interval) {
      self ! Fetch
    }
  }

  val receive: Receive = {
    case Fetch =>
      val fetched = fetchCompressed()
      cacheActor ! UpdateCache(fetched)
  }
}

class NetworkFetchActor(
    override val cacheActor: ActorRef
  ) extends FetchActor with NetworkItemsFetcher

