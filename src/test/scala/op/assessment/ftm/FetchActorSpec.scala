package op.assessment.ftm

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import op.assessment.ftm.CacheActor.UpdateCache
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object FetchActorSpec {

  trait FakeCompressor extends Compressor {
    override def compress[A](as: Seq[A]): Seq[Compressed[A]] = {
      as.map(Single(_))
    }
  }

  trait FakeItemsFetcherSuccess extends ItemsFetcher with FakeCompressor {
    override def fetch(): Try[Seq[String]] = Success(List("A", "B", "C"))
  }

  trait FakeItemsFetcherFailure extends ItemsFetcher with FakeCompressor {
    override def fetch(): Try[Seq[String]] = Failure {
      new RuntimeException("Test exception")
    }
  }

  class TestFetchActor(
      override val cacheActor: ActorRef
    ) extends FetchActor with FakeItemsFetcherSuccess

  class FailingFetchActor(
      override val cacheActor: ActorRef
    ) extends FetchActor with FakeItemsFetcherFailure

  def props(cacheActor: ActorRef)(fa: ActorRef => FetchActor): Props = {
    Props(fa(cacheActor))
  }
}

class FetchActorSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("FetchActorSpec"))

  import FetchActorSpec._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "FetchActor" should {
    "fetch every 1 second and send data to CacheActor" in {
      val probe = TestProbe()
      system.actorOf(props(cacheActor = probe.ref)(new TestFetchActor(_)))

      val fetched = List(Single("A"), Single("B"), Single("C"))
      val update = UpdateCache(fetched)

      probe.expectMsgAllOf(
        max = 2.2 seconds,
        update, update, update
      )

      probe.expectNoMessage(0.4 seconds)
    }
    "handle fetch errors" in {
      val probe = TestProbe()
      system.actorOf(props(cacheActor = probe.ref)(new FailingFetchActor(_)))
      probe.expectNoMessage()
    }
  }
}
