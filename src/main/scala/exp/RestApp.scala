package exp

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import exp.model.Model
import exp.model.Model.{CalculateRequest, CalculateResult, Expense}
import exp.service.{CalculateService, ExpenseService, NotesService}
import exp.web.CalculatePartialEndpoints.CalculationError
import exp.web.{Authentication, CalculateFullEndpoints, CalculatePartialEndpoints, ExpenseLogic, ExpensePartialEndpoints, SummaryLogic, SummaryPartialEndpoints}
import exp.web.ExpensePartialEndpoints.Other
import exp.web.ExpensePartialEndpoints.RequestError
import org.http4s.server.Router
import org.http4s.ember.server.*
import sttp.tapir.*
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.ExecutionContext
import sttp.tapir.generic.auto._

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

  private val summary = SummaryPartialEndpoints.summary.serverLogic {
    _ => {
      case (since, until) => expenseService.summary(since, until).map(Right(_))
    }
  }

  private val summaryPurpose = SummaryPartialEndpoints.summaryPurpose.serverLogic {
    _ => {
      case (since, until, c) => expenseService.summary(since, until).map(Right(_))
    }
  }
  private val summaryPurposeNote = SummaryPartialEndpoints.summaryPurposeNotes.serverLogic {
    _ => {
      case (since, until, _, _) => expenseService.summary(since, until).map(Right(_))
    }
  }

  private val summaryNote = SummaryPartialEndpoints.summaryNote.serverLogic {
    _ => {
      case (since, until, c) => expenseService.summary(since, until).map(Right(_))
    }
  }
  private val summaryNotePurpose = SummaryPartialEndpoints.summaryPurposeNotes.serverLogic {
    _ => {
      case (since, until, _, _) => expenseService.summary(since, until).map(Right(_))
    }
  }


  import org.http4s.dsl.io._

  val redirect: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => MovedPermanently("ups!", "Location" -> "index.html")
  }

  val swagger: List[ServerEndpoint[Any, IO]] = SwaggerInterpreter().fromEndpoints[IO](ExpensePartialEndpoints.endpoints :::
    SummaryPartialEndpoints.endpoints, "swagger", "v1")
  val swaggerRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(swagger)

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
        summary ::
        summaryPurpose ::
        summaryPurposeNote ::
        summaryNote ::
        summaryNotePurpose ::
        static ::
        Nil
    ),
    "" -> swaggerRoutes //swagger is on :8080/doc

  )


  override def run(args: List[String]): IO[ExitCode] = {
    // starting the server
    import org.http4s.implicits._

    IO.println("Starting server") *>
      EmberServerBuilder.default[IO]
        .withHost(Host.fromString("0.0.0.0").get)
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(router.orNotFound)
        .build
        .use(_ => IO.never[ExitCode])
        .as(ExitCode.Success)
  }

}
