package op.assessment.ftm

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import op.assessment.ftm.CacheActor.{GetItem, Item}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

class ApiRouteSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest
  with ApiRoutes with ActorsComponent {

  val probe = TestProbe()
  override def fetchActor: ActorRef = probe.ref
  override def cacheActor: ActorRef = TestProbe().ref

  probe.setAutoPilot(
    (sender: ActorRef, msg: Any) => {
      msg match {
        case GetItem(_) => Item("K")
      }
      TestActor.KeepRunning
    }
  )

  "ApiRoutes" should {
    "return 200 Ok" in {

      val request: HttpRequest = Get("/10")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should === (ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"item":"K"}""".stripMargin)
      }
    }
  }
}
