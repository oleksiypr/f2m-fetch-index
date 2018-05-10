package op.assessment.ftm

import akka.actor.{Actor, ActorRef}

trait ActorsComponent {
  def fetchActor: ActorRef
  def cacheActor: ActorRef
}

object CacheActor {
  final case class GetItem(i: Int)
  final case class Item(item: String)
}

class CacheActor extends Actor {
  val receive: Receive = {
    case _ => ???
  }
}

