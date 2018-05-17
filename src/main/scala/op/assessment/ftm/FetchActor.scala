package op.assessment.ftm

import java.net.URL
import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import op.assessment.ftm.CacheActor.UpdateCache
import op.assessment.ftm.FetchActor.Fetch
import scala.concurrent.duration.{FiniteDuration, _}
import scala.io.Source

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
  val url: URL
  override def fetch(): Seq[String] = {
    Source.fromURL(url).getLines().toSeq
  }
}

trait FetchActor extends Actor with ItemsFetcher {

  val cacheActor: ActorRef

  val log = Logging(context.system, this)

  val interval: FiniteDuration = {
    val config = context.system.settings.config
    config.getDouble("fetch.interval.seconds")
  }.seconds

  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.schedule(0.seconds, interval) {
      self ! Fetch
    }
    log.debug("Fetching started")
  }

  val receive: Receive = {
    case Fetch =>
      val fetched = fetchCompressed()
      log.debug("Fetched")
      cacheActor ! UpdateCache(fetched)
  }
}

class NetworkFetchActor(
    override val cacheActor: ActorRef
  ) extends FetchActor with NetworkItemsFetcher {

  override val url: URL = {
    val config = context.system.settings.config
    new URL(config.getString("fetch.url"))
  }
}

