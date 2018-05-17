package op.assessment.ftm

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import op.assessment.ftm.CacheActor.{GetItem, ItemFound, ItemNotFound, UpdateCache}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

object CacheActorSpec {

  trait FakeCompressor extends Compressor {
    override def decompress[A](as: Seq[Compressed[A]]): Seq[A] = {
      as map {
        case Single(v) => v
        case Repeat(_, v) => v
      }
    }
  }
}

class CacheActorSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("CacheActorSpec"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "CacheActor" should {
    "process cached items" in {
      val cacheActor = system.actorOf(CacheActor.props)

      cacheActor ! GetItem(0)
      expectMsg(ItemNotFound)

      val items = Vector(Single("A"), Single("B"), Single("C"))
      cacheActor ! UpdateCache(items)

      cacheActor ! GetItem(0)
      expectMsg(ItemFound("A"))

      cacheActor ! GetItem(1)
      expectMsg(ItemFound("B"))

      cacheActor ! GetItem(2)
      expectMsg(ItemFound("C"))

      cacheActor ! GetItem(3)
      expectMsg(ItemNotFound)
    }

    "update cache" in {
      val cacheActor = system.actorOf(CacheActor.props)

      cacheActor ! UpdateCache(Vector(Single("A")))
      cacheActor ! GetItem(0)
      expectMsg(ItemFound("A"))

      cacheActor ! UpdateCache(Vector(Single("B")))
      cacheActor ! GetItem(0)
      expectMsg(ItemFound("B"))
    }
  }
}
