package exp

import cats.effect._
import cats.syntax.all._
import exp.model.Model.{CalculateRequest, CalculateResult, Expense}
import exp.service.{CalculateService, ExpenseService}
import exp.web.CalculateEndpoints.CalculationError
import exp.web.{CalculateEndpoints, ExpenseEndpoints}
import exp.web.ExpenseEndpoints.Other
import exp.web.ExpenseEndpoints.RequestError
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
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
      .get
      .serverLogic(user => id => expenseService.find(Expense.Id(id)).map(_.toRight(Other("Not found"))))

  private val editExpense =
    ExpenseEndpoints.edit.serverLogic(user => { case (id, expense) => expenseService.edit(expense).map(_.asRight[RequestError]) })
  private val addExpense = ExpenseEndpoints.add.serverLogic(user => expense => expenseService.add(expense).map(_.asRight[RequestError]))
  private val deleteExpense =
    ExpenseEndpoints.delete.serverLogic(user => id => expenseService.delete(Expense.Id(id)).map(_.asRight[RequestError]))

  private val calculate = CalculateEndpoints.calculate
    .serverLogic(_ => request => CalculateService.calculate(request).asRight[CalculationError].pure[IO])

  import org.http4s.dsl.io._

  val redirect: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => MovedPermanently("ups!", "Location" -> "index.html")
  }

  val router: HttpRoutes[IO] = Router(
    "" -> Http4sServerInterpreter[IO]().toRoutes(
        getExpense ::
        addExpense ::
        deleteExpense ::
        editExpense ::
          calculate::
        static ::
        Nil
    )
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
