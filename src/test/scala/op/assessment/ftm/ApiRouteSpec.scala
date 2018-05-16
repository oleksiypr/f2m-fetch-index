package op.assessment.ftm

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import op.assessment.ftm.CacheActor.{GetItem, ItemFound, ItemNotFound}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

class ApiRouteSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest
  with ApiRoutes with ActorsComponent {

  val probe = TestProbe()
  override def fetchActor: ActorRef = TestProbe().ref
  override def cacheActor: ActorRef = probe.ref

  val TestCacheSize = 10

  probe.setAutoPilot(
    (sender: ActorRef, msg: Any) => {
      msg match {
        case GetItem(n) if n < TestCacheSize => sender ! ItemFound("K")
        case GetItem(_) => sender ! ItemNotFound
      }
      TestActor.KeepRunning
    }
  )

  "ApiRoutes" should {
    "return 200 Ok" in {

      val request: HttpRequest = Get("/5")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should === (ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"value":"K"}""".stripMargin)
      }
    }

    "return 404 Not found" in {
      val request: HttpRequest = Get("/12")

      request ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }
  }
}
