package op.assessment.ftm

import akka.actor.{Actor, ActorRef}

trait ActorsComponent {
  def fetchActor: ActorRef
  def cacheActor: ActorRef
}
