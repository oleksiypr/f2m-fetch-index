package op.assessment.ftm

import akka.actor.ActorRef

trait ActorsComponent {
  def fetchActor: ActorRef
  def cacheActor: ActorRef
}
