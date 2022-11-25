package exp.web

import cats.effect.IO
import exp.model.Model.*
import exp.web.Authentication.AuthenticationError
import exp.web.CalculatePartialEndpoints.CalculationError
import exp.web.TapirCodecs.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint

object SearchPartialEndpoints {

  trait RequestError

  case class RequestAuthenticationError(wrapped: AuthenticationError)
      extends RequestError

  case class Other(msg: String) extends RequestError

  private val secureEndpoint = Authentication.secureEndpoint
    .mapErrorOut(RequestAuthenticationError.apply)(_.wrapped)
    .errorOutVariant[RequestError](oneOfVariant(stringBody.mapTo[Other]))

  private val baseEndpoint = secureEndpoint
    .in("api" / "expenses" / "search" / "from")
    .in(path[Date]("since").example(Date(2022, 2)))
    .in("to")
    .in(path[Date]("until").example(Date(2022, 5)))
    .out(jsonBody[List[ExpenseSummary]]) // TODO ExpenseReport

  val search = baseEndpoint

  val searchPurpose = baseEndpoint
    .in("purpose")
    .in(path[Purpose]("purpose"))

  val searchNote = baseEndpoint
    .in("note")
    .in(path[Note]("note"))

  val searchPurposeNotes = baseEndpoint
    .in("purpose")
    .in(path[Purpose]("purpose"))
    .in("note")
    .in(path[Note]("note"))

  val searchNotePurpose = baseEndpoint
    .in("note")
    .in(path[Note]("note"))
    .in("purpose")
    .in(path[Purpose]("purpose"))

  val endpoints: List[AnyEndpoint] = List(
    search,
    searchNote,
    searchNotePurpose,
    searchPurpose,
    searchPurposeNotes
  ).map(_.endpoint)

}
