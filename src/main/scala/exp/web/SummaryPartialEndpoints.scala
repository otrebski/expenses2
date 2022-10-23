package exp.web

import exp.web.Authentication.AuthenticationError
import sttp.tapir.{oneOfVariant, stringBody}
import TapirCodecs.*
import cats.effect.IO
import exp.model.Model.{CalculateRequest, CalculateResult, Date, ExpenseSummary, Note, Purpose}
import exp.web.CalculatePartialEndpoints.CalculationError
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

object SummaryPartialEndpoints {
  //  Summary:
  //    - "api" / "summary" / "from" / Segment / "to" / Segment
  //    - "purpose" / Segment / "note" / Segment
  //    - "note" / Segment / "purpose" / Segment
  //    - "purpose" / Segment
  //    - "note" / Segment
  //https://host.pl/api/expenses/search/from/2022-1/to/2022-7/note/Pentliczek/purpose/maczeta
  //https://host.pl/api/expenses/search/from/2022-7/to/2022-7
  //https://host.pl/api/summary/from/2022-01/to/2022-12

  trait RequestError

  case class RequestAuthenticationError(wrapped: AuthenticationError) extends RequestError

  case class Other(msg: String) extends RequestError

  private val secureEndpoint = Authentication.secureEndpoint
    .mapErrorOut(RequestAuthenticationError.apply)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))

  private val apiEndpoint = secureEndpoint.in("api")


  val summary: PartialServerEndpoint[UsernamePassword, Authentication.User, (Date, Date), RequestError, List[ExpenseSummary], Any, IO] = apiEndpoint
    .in("summary" / "from")
    .in(path[Date]("Starting date").example(Date(2022, 2)))
    .in("to")
    .in(path[Date]("Ending date").example(Date(2022, 3)))
    .out(jsonBody[List[ExpenseSummary]])

  val summaryPurpose = summary
    .in("purpose").in(path[Purpose]("purpose"))

  val summaryNote = summary
    .in("note").in(path[Note]("note"))


  val summaryPurposeNotes = summary
    .in("purpose").in(path[Purpose]("purpose")).in("note").in(path[Note]("note"))

  val summaryNotePurpose = summary
    .in("note").in(path[Note]("note")).in("purpose").in(path[Purpose]("purpose"))


  val endpoints: List[AnyEndpoint] = List(summary, summaryNote, summaryNotePurpose, summaryPurpose, summaryPurposeNotes).map(_.endpoint)

}
