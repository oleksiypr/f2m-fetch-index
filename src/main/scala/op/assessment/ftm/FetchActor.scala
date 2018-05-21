package op.assessment.ftm

import java.net.URL
import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import op.assessment.ftm.CacheActor.UpdateCache
import op.assessment.ftm.FetchActor.Fetch
import scala.concurrent.duration.{FiniteDuration, _}
import scala.io.Source
import scala.util.{Failure, Success, Try}

object FetchActor {

  def props(cacheActor: ActorRef): Props = Props(
    new NetworkFetchActor(cacheActor)
  )

  case object Fetch
}

trait ItemsFetcher extends Compressor {
  def fetch(): Try[Seq[String]]
  def fetchCompressed(): Try[Seq[Compressed[String]]] = fetch().map(compress)
}

trait NetworkItemsFetcher extends ItemsFetcher {
  val url: URL
  override def fetch() = Try {
    Source.fromURL(url).getLines().toVector
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
      fetchCompressed() match {
        case Success(fetched) =>
          log.debug(s"Fetched and compressed to size: ${fetched.size}")
          cacheActor ! UpdateCache(fetched)
        case Failure(th) => log.error(th, "Failed to fetch")
      }
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

