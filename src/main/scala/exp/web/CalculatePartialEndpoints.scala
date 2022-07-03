package exp.web

import cats.effect.IO
import exp.model.Model.CalculateRequest
import exp.model.Model.CalculateResult
import exp.web.Authentication.AuthenticationError
import sttp.tapir.{oneOfVariant, stringBody}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint

object CalculatePartialEndpoints {

  sealed trait CalculationError

  case class RequestAuthenticationError(wrapped: AuthenticationError) extends CalculationError

  case class Other(msg: String) extends CalculationError

  private val secureEndpoint = Authentication.secureEndpoint
    .mapErrorOut(RequestAuthenticationError.apply)(_.wrapped)
    .errorOutVariant[CalculationError](oneOfVariant(stringBody.mapTo[Other]))

  val calculate: PartialServerEndpoint[UsernamePassword, Authentication.User, CalculateRequest, CalculationError, CalculateResult, Any, IO] = secureEndpoint
    .in("api" / "calculate")
    .in(jsonBody[CalculateRequest])
    .out(jsonBody[CalculateResult])


}
