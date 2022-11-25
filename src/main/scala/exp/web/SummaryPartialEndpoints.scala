package exp.web

import TapirCodecs.*
import cats.effect.IO
import exp.model.Model.CalculateRequest
import exp.model.Model.CalculateResult
import exp.model.Model.Date
import exp.model.Model.ExpenseSummary
import exp.model.Model.Note
import exp.model.Model.Purpose
import exp.web.Authentication.AuthenticationError
import exp.web.CalculatePartialEndpoints.CalculationError
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.UsernamePassword
import sttp.tapir.oneOfVariant
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.stringBody

object SummaryPartialEndpoints {

  trait RequestError

  case class RequestAuthenticationError(wrapped: AuthenticationError)
      extends RequestError

  case class Other(msg: String) extends RequestError

  private val secureEndpoint = Authentication.secureEndpoint
    .mapErrorOut(RequestAuthenticationError.apply)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))

  private val apiEndpoint = secureEndpoint.in("api")

  val summary: PartialServerEndpoint[
    UsernamePassword,
    Authentication.User,
    (Date, Date),
    RequestError,
    List[ExpenseSummary],
    Any,
    IO
  ] = apiEndpoint
    .in("summary" / "from")
    .in(path[Date]("Starting date").example(Date(2022, 2)))
    .in("to")
    .in(path[Date]("Ending date").example(Date(2022, 3)))
    .out(jsonBody[List[ExpenseSummary]]) // TODO ExpenseReport

  val summaryPurpose = summary
    .in("purpose")
    .in(path[Purpose]("purpose"))

  val summaryNote = summary
    .in("note")
    .in(path[Note]("note"))

  val summaryPurposeNotes = summary
    .in("purpose")
    .in(path[Purpose]("purpose"))
    .in("note")
    .in(path[Note]("note"))

  val summaryNotePurpose = summary
    .in("note")
    .in(path[Note]("note"))
    .in("purpose")
    .in(path[Purpose]("purpose"))

  val endpoints: List[AnyEndpoint] = List(
    summary,
    summaryNote,
    summaryNotePurpose,
    summaryPurpose,
    summaryPurposeNotes
  ).map(_.endpoint)

}
