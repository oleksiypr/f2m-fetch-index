package op.assessment.ftm

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import op.assessment.ftm.ApiRoutes.{ErrorRsp, Item}
import op.assessment.ftm.CacheActor.{GetItem, ItemFound, ItemNotFound, QueryResult}
import spray.json.DefaultJsonProtocol._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ApiRoutes {
  final case class Item(value: String)
  final case class ErrorRsp(msg: String)
}

trait ApiRoutes extends SprayJsonSupport { this: ActorsComponent =>

  implicit val timeout: Timeout = 5.seconds
  implicit val itemFormat = jsonFormat1(Item)
  implicit val errFormat = jsonFormat1(ErrorRsp)

  lazy val routes: Route = path(IntNumber) {
    index => {
      val result = cacheActor ? GetItem(index)
      onComplete(result.mapTo[QueryResult]) {
        case Success(ItemFound(v)) =>
          complete((StatusCodes.OK, Item(v)))
        case Success(ItemNotFound) =>
          complete((StatusCodes.NotFound, ErrorRsp("Item not found")))
        case Failure(err) =>
          err.printStackTrace()
          complete((
            StatusCodes.InternalServerError,
            ErrorRsp(err.getMessage)))
      }
    }
  }
}
