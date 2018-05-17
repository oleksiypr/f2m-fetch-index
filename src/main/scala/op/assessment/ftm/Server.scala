package op.assessment.ftm

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import scala.io.StdIn

object Server extends App with ApiRoutes with ActorsComponent {

  implicit val system = ActorSystem("ItemsFetchingSystem")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  override val cacheActor = system.actorOf(CacheActor.props)
  override val fetchActor = system.actorOf(FetchActor.props(cacheActor))

  val port: Int = system.settings.config.getInt("server.port")

  val serverBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(routes, "localhost", port)

  println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")

  StdIn.readLine()

  serverBindingFuture
    .flatMap(_.unbind())
    .onComplete { done =>
      done.failed.map { ex => ex.printStackTrace() }
      system.terminate()
    }
}
