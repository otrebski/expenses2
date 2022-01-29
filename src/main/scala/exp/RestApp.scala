package exp

import cats.effect._
import cats.syntax.all._
import exp.model.Model.{CalculateRequest, CalculateResult, Expense}
import exp.service.{CalculateService, ExpenseService, NotesService}
import exp.web.CalculatePartialEndpoints.CalculationError
import exp.web.{CalculateFullEndpoints, CalculatePartialEndpoints, ExpenseLogic, ExpensePartialEndpoints}
import exp.web.ExpensePartialEndpoints.Other
import exp.web.ExpensePartialEndpoints.RequestError
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext

object RestApp extends IOApp {
  // the endpoint: single fixed path input ("hello"), single query parameter
  // corresponds to: GET /hello?name=...

  val expenseService: ExpenseService[IO] = ExpenseService.mockInstance[IO]()
  val notesService: NotesService[IO] = NotesService.mockInstance[IO]()

  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  import org.http4s._

  val static: ServerEndpoint[Any, IO] = resourcesGetServerEndpoint[IO](endpoint.input)(this.getClass.getClassLoader, "static")

  private val getExpense = ExpensePartialEndpoints.get.serverLogic(ExpenseLogic.get(expenseService))

  private val editExpense = ExpensePartialEndpoints.edit.serverLogic(ExpenseLogic.edit(expenseService))
  private val addExpense = ExpensePartialEndpoints.add.serverLogic(ExpenseLogic.add(expenseService))
  private val deleteExpense = ExpensePartialEndpoints.delete.serverLogic(ExpenseLogic.delete(expenseService))

  private val monthSummary = ExpensePartialEndpoints.listInterval.serverLogic(ExpenseLogic.monthSummary(expenseService))

  private val notes = ExpensePartialEndpoints.notes.serverLogic(ExpenseLogic.notes(notesService))
  private val allPurposes = ExpensePartialEndpoints.allPurposes.serverLogic(ExpenseLogic.allPurposes(expenseService))
  private val purposes = ExpensePartialEndpoints.purposes.serverLogic(ExpenseLogic.purposes(expenseService))


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
        monthSummary ::
        notes ::
        purposes ::
        allPurposes ::
        CalculateFullEndpoints.calculate ::
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
