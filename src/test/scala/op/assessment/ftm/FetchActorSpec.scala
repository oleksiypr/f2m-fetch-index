package op.assessment.ftm

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import op.assessment.ftm.CacheActor.UpdateCache
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

object FetchActorSpec {

  trait FakeCompressor extends Compressor {
    override def compress[A](as: Seq[A]): Seq[Compressed[A]] = {
      as.map(Single(_))
    }
  }

  trait FakeItemsFetcher extends ItemsFetcher with FakeCompressor {
    override def fetch(): Seq[String] = List("A", "B", "C")
  }

  class TestFetchActor(
      override val cacheActor: ActorRef
    ) extends FetchActor with FakeItemsFetcher

  def props(cacheActor: ActorRef): Props = {
    Props(new TestFetchActor(cacheActor))
  }
}

class FetchActorSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("FetchActorSpec"))

  import FetchActorSpec._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "FetchActor" should {
    "fetch every 1 second and send data to CacheActor" in {
      val probe = TestProbe()
      system.actorOf(props(cacheActor = probe.ref))

      val fetched = List(Single("A"), Single("B"), Single("C"))
      val update = UpdateCache(fetched)

      probe.expectMsgAllOf(
        max = 2.2 seconds,
        update, update, update
      )

      probe.expectNoMessage(0.4 seconds)
    }
  }
}
