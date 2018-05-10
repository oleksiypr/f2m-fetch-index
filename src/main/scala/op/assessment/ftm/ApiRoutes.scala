package op.assessment.ftm


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import op.assessment.ftm.ApiRoutes.Item
import spray.json.DefaultJsonProtocol._

object ApiRoutes {
  final case class Item(item: String)
}

trait ApiRoutes extends SprayJsonSupport {

  implicit val timeout: Timeout = 5.seconds
  implicit val itemFormat = jsonFormat1(Item)

  lazy val routes: Route = path(Segment) {
    index => {
      complete((StatusCodes.OK, Item("K")))
    }
  }

}
