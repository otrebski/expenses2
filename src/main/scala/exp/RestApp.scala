package exp

import scala.concurrent.ExecutionContext

import org.http4s.ember.server.*
import org.http4s.server.Router

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import exp.model.Model
import exp.model.Model.CalculateRequest
import exp.model.Model.CalculateResult
import exp.model.Model.Expense
import exp.service.CalculateService
import exp.service.ExpenseService
import exp.service.NotesService
import exp.web.Authentication
import exp.web.CalculateFullEndpoints
import exp.web.CalculatePartialEndpoints
import exp.web.CalculatePartialEndpoints.CalculationError
import exp.web.ExpenseLogic
import exp.web.ExpensePartialEndpoints
import exp.web.ExpensePartialEndpoints.Other
import exp.web.ExpensePartialEndpoints.RequestError
import exp.web.SearchPartialEndpoints
import exp.web.SummaryLogic
import exp.web.SummaryPartialEndpoints
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object RestApp extends IOApp {
  // the endpoint: single fixed path input ("hello"), single query parameter
  // corresponds to: GET /hello?name=...

  val expenseService: ExpenseService[IO] = ExpenseService.mockInstance[IO]()
  val notesService: NotesService[IO] = NotesService.mockInstance[IO]()

  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  import org.http4s._

  val static: ServerEndpoint[Any, IO] = resourcesGetServerEndpoint[IO](
    endpoint.input
  )(this.getClass.getClassLoader, "static")

  private val getExpense =
    ExpensePartialEndpoints.get.serverLogic(ExpenseLogic.get(expenseService))

  private val editExpense =
    ExpensePartialEndpoints.edit.serverLogic(ExpenseLogic.edit(expenseService))
  private val addExpense =
    ExpensePartialEndpoints.add.serverLogic(ExpenseLogic.add(expenseService))
  private val deleteExpense = ExpensePartialEndpoints.delete.serverLogic(
    ExpenseLogic.delete(expenseService)
  )

  private val monthSummary = ExpensePartialEndpoints.listInterval.serverLogic(
    ExpenseLogic.monthSummary(expenseService)
  )

  private val notes =
    ExpensePartialEndpoints.notes.serverLogic(ExpenseLogic.notes(notesService))
  private val allPurposes = ExpensePartialEndpoints.allPurposes.serverLogic(
    ExpenseLogic.allPurposes(expenseService)
  )
  private val purposes = ExpensePartialEndpoints.purposes.serverLogic(
    ExpenseLogic.purposes(expenseService)
  )

  private val summary = SummaryPartialEndpoints.summary.serverLogic { _ =>
    { case (since, until) =>
      expenseService.summary(since, until).map(Right(_))
    }
  }

  private val summaryPurpose =
    SummaryPartialEndpoints.summaryPurpose.serverLogic { _ =>
      { case (since, until, c) =>
        expenseService.summary(since, until).map(Right(_))
      }
    }
  private val summaryPurposeNote =
    SummaryPartialEndpoints.summaryPurposeNotes.serverLogic { _ =>
      { case (since, until, _, _) =>
        expenseService.summary(since, until).map(Right(_))
      }
    }

  private val summaryNote = SummaryPartialEndpoints.summaryNote.serverLogic {
    _ =>
      { case (since, until, c) =>
        expenseService.summary(since, until).map(Right(_))
      }
  }
  private val summaryNotePurpose =
    SummaryPartialEndpoints.summaryPurposeNotes.serverLogic { _ =>
      { case (since, until, _, _) =>
        expenseService.summary(since, until).map(Right(_))
      }
    }

  private val search = SearchPartialEndpoints.search.serverLogic { _ =>
    { case (since, until) =>
      expenseService.search(since, until).map(Right(_))
    }
  }

  private val searchPurpose = SearchPartialEndpoints.searchPurpose.serverLogic {
    _ =>
      { case (since, until, purpose) =>
        expenseService
          .search(since, until, purpose = Some(purpose))
          .map(Right(_))
      }
  }
  private val searchPurposeNote =
    SearchPartialEndpoints.searchPurposeNotes.serverLogic { _ =>
      { case (since, until, purpose, note) =>
        expenseService
          .search(since, until, Some(purpose), Some(note))
          .map(Right(_))
      }
    }

  private val searchNote = SearchPartialEndpoints.searchNote.serverLogic { _ =>
    { case (since, until, note) =>
      expenseService.search(since, until, note = Some(note)).map(Right(_))
    }
  }
  private val searchNotePurpose =
    SearchPartialEndpoints.searchPurposeNotes.serverLogic { _ =>
      { case (since, until, purpose, note) =>
        expenseService
          .search(since, until, Some(purpose), Some(note))
          .map(Right(_))
      }
    }

  import org.http4s.dsl.io._

  val redirect: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root =>
    MovedPermanently("ups!", "Location" -> "index.html")
  }

  val swagger: List[ServerEndpoint[Any, IO]] =
    SwaggerInterpreter().fromEndpoints[IO](
      ExpensePartialEndpoints.endpoints :::
        SummaryPartialEndpoints.endpoints :::
        SearchPartialEndpoints.endpoints :::
        List(CalculateFullEndpoints.calculate.endpoint),
      "swagger",
      "v1"
    )

  val swaggerRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(swagger)

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
        search ::
        searchPurpose ::
        searchPurposeNote ::
        searchNote ::
        searchNotePurpose ::
        static ::
        Nil
    ),
    "" -> swaggerRoutes // swagger is on :8080/doc

  )

  override def run(args: List[String]): IO[ExitCode] = {
    // starting the server
    import org.http4s.implicits._

    IO.println("Starting server") *>
      EmberServerBuilder
        .default[IO]
        .withHost(Host.fromString("0.0.0.0").get)
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(router.orNotFound)
        .build
        .use(_ => IO.never[ExitCode])
        .as(ExitCode.Success)
  }

}
