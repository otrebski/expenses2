package exp

import cats.effect._
import cats.syntax.all._
import exp.web.Endpoints
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.model.HeaderNames
import sttp.tapir._
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext

object HelloWorldHttp4sServer extends IOApp {
  // the endpoint: single fixed path input ("hello"), single query parameter
  // corresponds to: GET /hello?name=...
  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  val auth: UsernamePassword => IO[Either[String, String]] = {
    case UsernamePassword("john", Some("pass")) => IO("john".asRight)
    case _                                      => IO(Left("(unit error)"))
  }

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  import org.http4s._
  val static: ServerEndpoint[Any, IO] = resourcesGetServerEndpoint[IO](endpoint.input)(this.getClass.getClassLoader, "static")

  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val helloWorldRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      List(
        helloWorld.serverLogic(name => IO(s"Hello, $name!".asRight[Unit])),
        static,
        Endpoints
          .list
          .serverSecurityLogic(auth)
          .serverLogic(username => _ => IO.println(s"Request for $username") *> IO(List(Endpoints.book).asRight[String])),
        Endpoints
          .get
          .serverSecurityLogic(auth)
          .serverLogic(username => _ => IO.println(s"Request for $username") *> IO(Endpoints.book.asRight[String]))
      )
    )

  import org.http4s.dsl.io._

  val redirect: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => MovedPermanently("ups!", "Location" -> "index.html")
  }

  override def run(args: List[String]): IO[ExitCode] =
    // starting the server
    IO.println("Starting server") *>
      BlazeServerBuilder[IO]
        .withExecutionContext(ec)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(Router("/" -> helloWorldRoutes, "" -> redirect).orNotFound)
        .resource
        .use { _ =>
          IO.never[ExitCode]
        }
        .as(ExitCode.Success)

}
