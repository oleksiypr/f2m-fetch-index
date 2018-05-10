package op.assessment.ftm

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

class ApiRouteSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest
  with ApiRoutes {

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
