package exp

import cats.conversions.all.autoConvertProfunctorVariance
import cats.data.Kleisli
import cats.effect._
import cats.syntax.all._
import exp.model.Model
import exp.model.Model.Date
import exp.model.Model.Expense
import exp.model.Model.Note
import exp.model.Model.Purpose
import exp.service.ExpenseService
import exp.web.ExpenseEndpoints
import exp.web.ExpenseEndpoints.Other
import exp.web.ExpenseEndpoints.RequestError
import exp.web.ExpenseEndpoints.editExpense
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir._
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext

object HelloWorldHttp4sServer extends IOApp {
  // the endpoint: single fixed path input ("hello"), single query parameter
  // corresponds to: GET /hello?name=...

  val expenseService = ExpenseService.mockInstance[IO]()
  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  import org.http4s._
  val static: ServerEndpoint[Any, IO] = resourcesGetServerEndpoint[IO](endpoint.input)(this.getClass.getClassLoader, "static")

  private val getExpense =
    ExpenseEndpoints
      .getExpense
      .serverLogic(user => id => expenseService.find(Expense.Id(id)).map(_.toRight(Other("Not found"))))

  private val editExpense =
    ExpenseEndpoints.editExpense.serverLogic(user => { case (id, expense) => expenseService.edit(expense).map(_.asRight[RequestError]) })
  private val addExpense = ExpenseEndpoints.addExpense.serverLogic(user => expense => expenseService.add(expense).map(_.asRight[RequestError]))
  private val deleteExpense =
    ExpenseEndpoints.deleteExpense.serverLogic(user => id => expenseService.delete(Expense.Id(id)).map(_.asRight[RequestError]))

  private val endpoints: List[ServerEndpoint[Any, IO]] = List(
    helloWorld.serverLogic(name => IO(s"Hello, $name!".asRight[Unit])),
    static,
    getExpense,
    editExpense,
    addExpense,
    deleteExpense
  )

// converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val apiRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(endpoints)

  import org.http4s.dsl.io._

  val redirect: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => MovedPermanently("ups!", "Location" -> "index.html")
  }

//  val router: HttpRoutes[IO] = Router("" -> apiRoutes, "" -> redirect)
  val router: HttpRoutes[IO] = Router(
    "" -> Http4sServerInterpreter[IO]().toRoutes(
      listBooks ::
        getExpense ::
        addExpense ::
        deleteExpense ::
        editExpense ::
        static ::
        Nil
    )
//    "" -> Http4sServerInterpreter[IO]().toRoutes(getExpense)
  )

  override def run(args: List[String]): IO[ExitCode] =
    // starting the server
    IO.println("Starting server") *>
      BlazeServerBuilder[IO]
        .withExecutionContext(ec)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(router.orNotFound)
        .resource
        .use { _ =>
          IO.never[ExitCode]
        }
        .as(ExitCode.Success)

}
