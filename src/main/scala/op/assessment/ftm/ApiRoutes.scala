package op.assessment.ftm

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import op.assessment.ftm.ApiRoutes.ErrorRsp
import op.assessment.ftm.CacheActor.{GetItem, Item}
import spray.json.DefaultJsonProtocol._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ApiRoutes {
  final case class ErrorRsp(msg: String)
}

trait ApiRoutes extends SprayJsonSupport { this: ActorsComponent =>

  implicit val timeout: Timeout = 5.seconds
  implicit val itemFormat = jsonFormat1(Item)
  implicit val errFormat = jsonFormat1(ErrorRsp)

  lazy val routes: Route = path(IntNumber) {
    index => {
      val result = cacheActor ? GetItem(index)
      onComplete(result.mapTo[Item]) {
        case Success(item) =>
          complete((StatusCodes.OK, item))
        case Failure(err) =>
          err.printStackTrace()
          complete((
            StatusCodes.InternalServerError,
            ErrorRsp(err.getMessage)))
      }
      complete((StatusCodes.OK, Item("K")))
    }
  }
}
